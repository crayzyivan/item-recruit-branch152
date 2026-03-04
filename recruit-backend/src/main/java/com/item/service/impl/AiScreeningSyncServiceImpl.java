package com.item.service.impl;

import com.item.convert.CandidateJobConvert;
import com.item.dto.DictionaryDTO;
import com.item.dto.ScreeningOverviewDTO;
import com.item.entity.ApplicationScreeningReportsEntity;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.es.ResumeEsService;
import com.item.framework.constant.DictTypeConstans;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.service.AiScreeningSyncService;
import com.item.service.ApplicationScreeningReportsService;
import com.item.service.CandidateJobService;
import com.item.service.CandidateService;
import com.item.service.DictionaryService;
import com.item.service.JobService;
import com.item.service.migration.DataMigrationMappingService;
import com.item.util.JsonUtils;
import com.item.vo.ApplicationsSyncVO;
import com.item.vo.ai.JobMatchResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI筛选结果同步服务实现类
 * 
 * 实现AiScreeningSyncService接口，提供AI筛选结果同步的核心业务逻辑，
 * 包括从PostgreSQL查询筛选结果并同步到MySQL的r_candidate_job表。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class AiScreeningSyncServiceImpl implements AiScreeningSyncService {

    private final ApplicationScreeningReportsService applicationScreeningReportsService;
    private final CandidateJobService candidateJobService;
    private final CandidateService candidateService;
    private final JobService jobService;
    private final DictionaryService dictionaryService;
    private final ResumeEsService resumeEsService;
    private final DataMigrationMappingService  dataMigrationMappingService;

    @Override
    @Transactional
    public Boolean syncScreeningResult(Long candidateJobId, String applicationId) {
        log.info("Starting AI screening result sync for candidateJobId: {}, applicationId: {}", candidateJobId, applicationId);
        try {
            //校验是否已执行
//            boolean isExists = dataMigrationMappingService.existsByMysqlIdAndType(candidateJobId, MigrationBusTypeEnum.SCREENING_REPORTS);
//            if (isExists){
//                return true;
//            }
            // 1. 根据applicationId查询PostgreSQL筛选结果
            ApplicationScreeningReportsEntity latestReport = applicationScreeningReportsService.getReportByApplicationId(UUID.fromString(applicationId));
            // 2. 根据candidateJobId查询MySQL的r_candidate_job记录
            CandidateJobEntity candidateJob = candidateJobService.getById(candidateJobId);
            if (candidateJob == null) {
                log.info("Candidate job not found for candidateJobId: {}", candidateJobId);
                return false;
            }
            // 3. 映射recommendation和assessment_score字段
            candidateJob.setRecommendation(convertRecommendationToLong(latestReport.getRecommendation()));
            candidateJob.setAssessmentScore(latestReport.getAssessmentScore());
            candidateJobService.updateById(candidateJob);
            // 4.组装es数据
            JobMatchResultVO jobMatchResultVO=convertJobMatchResultVO(candidateJob,latestReport);
            resumeEsService.saveResumeAiResultToEs(jobMatchResultVO);
            // 5.记录迁移主键id对应表
            dataMigrationMappingService.saveMapping(String.valueOf(latestReport.getId()),candidateJobId,MigrationBusTypeEnum.SCREENING_REPORTS);
            log.info("Successfully synced AI screening result for candidateJobId: {}, applicationId: {}",
                    candidateJobId, applicationId);
            return true;
        } catch (Exception e) {
            log.error("Error occurred while syncing AI screening result for candidateJobId: {}, applicationId: {}", 
                candidateJobId, applicationId, e);
            return false;
        }
    }


    /**
     * 组装es ai筛选数据
     * @param candidateJob
     * @param latestReport
     * @return
     */
    private JobMatchResultVO convertJobMatchResultVO(CandidateJobEntity candidateJob,ApplicationScreeningReportsEntity latestReport){
        JobMatchResultVO jobMatchResultVO=new JobMatchResultVO();
        jobMatchResultVO.setId(candidateJob.getId());
        jobMatchResultVO.setRecommendation(candidateJob.getRecommendation());
        jobMatchResultVO.setAssessmentScore(candidateJob.getAssessmentScore());
        jobMatchResultVO.setCreateTime(latestReport.getScreeningDate().toLocalDateTime());
        jobMatchResultVO.setUpdateTime(LocalDateTime.now());
        jobMatchResultVO.setDeleted(0);
        jobMatchResultVO.setApplyStatus(candidateJob.getApplyStatus());
        jobMatchResultVO.setApplyStatusName(JobApplyStatus.fromCode(candidateJob.getApplyStatus()).getName());

        //所需字典
        Map<Long, String> degreeMap = dictionaryService
                .listByTypes(List.of(DictTypeConstans.SALARYTYPE,DictTypeConstans.CURRENCYTYPE))
                .stream()
                .collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue));
        //---候选人信息
        CandidateEntity candidateEntity = candidateService.getById(candidateJob.getCandidateId());
        CandidateJobConvert.INSTANCE.candidateEntityToJobMatchResultVO(candidateEntity, jobMatchResultVO);
        jobMatchResultVO.setCurrencyName(Objects.nonNull(candidateEntity.getCurrencyTypeId())?degreeMap.get(candidateEntity.getCurrencyTypeId()):"");

        //---职位信息
        JobEntity jobEntity = jobService.getById(candidateJob.getJobId());
        CandidateJobConvert.INSTANCE.jobEntityToJobMatchResultVO(jobEntity, jobMatchResultVO);
        jobMatchResultVO.setJobCurrencyName(Objects.nonNull(jobEntity.getCurrency())?degreeMap.get(Long.valueOf(jobEntity.getCurrency())):"");
        jobMatchResultVO.setSalaryTypeName(Objects.nonNull(jobEntity.getSalaryType())?degreeMap.get(Long.valueOf(jobEntity.getSalaryType())):"");

        //ai简历筛选评价
        ScreeningOverviewDTO screeningOverviewDTO=JsonUtils.toObject(latestReport.getOverview(), ScreeningOverviewDTO.class);
        if (screeningOverviewDTO!=null){
            jobMatchResultVO.setSummary(screeningOverviewDTO.getSummary());
            jobMatchResultVO.setAnalysis(screeningOverviewDTO.getAnalysis());
            jobMatchResultVO.setComments(screeningOverviewDTO.getComments());
            jobMatchResultVO.setStrengths(screeningOverviewDTO.getStrengths());
            jobMatchResultVO.setWeaknesses(screeningOverviewDTO.getWeaknesses());
        }
        return jobMatchResultVO;
    }

    /**
     * 将推荐结果字符串转换为Long类型
     * 
     * @param recommendation 推荐结果字符串
     * @return 转换后的Long值
     */
    private Long convertRecommendationToLong(String recommendation) {
        // 根据业务需求映射推荐结果
        return switch (recommendation.trim()) {
            case "Deny Outright" -> 2L;
            case "Proceed with Application" -> 1L;
            default -> null;
        };
    }

    /**
     * 批量同步
     * @param syncVo
     * @return
     */
    @Override
    public boolean aiScreeningSync(ApplicationsSyncVO syncVo) {
        List<ApplicationScreeningReportsEntity> reportsEntities = applicationScreeningReportsService.listBySyncVo(syncVo);
        if (CollectionUtils.isNotEmpty(reportsEntities)) {
            for (ApplicationScreeningReportsEntity reportsEntity:reportsEntities){
                Optional<DataMigrationMappingEntity> mappingEntityOptional = dataMigrationMappingService.findByPgsqlIdAndType(reportsEntity.getApplicationId(), MigrationBusTypeEnum.CANDIDATE_JOB);
                mappingEntityOptional.ifPresent(mapping -> syncScreeningResult(mapping.getMysqlId(), mapping.getPgsqlId()));
            }
        }
        return true;
    }

    /**
     * 未同步
     * @return
     */
    @Override
    public List<String> notAiScreeningSync() {
        List<String> applicationIds=new ArrayList<>();
        List<ApplicationScreeningReportsEntity> allApplicationIds= applicationScreeningReportsService.listBySyncVo(new ApplicationsSyncVO());
        List<DataMigrationMappingEntity> candidateJobList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.CANDIDATE_JOB);
        List<DataMigrationMappingEntity> screenList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.SCREENING_REPORTS);

        List<String> pgIds=screenList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();
        List<String> pgCandidateJobIds=candidateJobList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();

        for (ApplicationScreeningReportsEntity reportsEntity:allApplicationIds){
            if (!pgIds.contains(String.valueOf(reportsEntity.getId()))){
                if (pgCandidateJobIds.contains(reportsEntity.getApplicationId())){
                    applicationIds.add(reportsEntity.getApplicationId());
                }
            }
        }
        return applicationIds;
    }
}
