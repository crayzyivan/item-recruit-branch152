package com.item.service.impl;

import com.item.dto.job.LocationValRecordDTO;
import com.item.entity.ApplicationInterviewReportsEntity;
import com.item.entity.ApplicationScreeningReportsEntity;
import com.item.entity.ApplicationStageLogEntity;
import com.item.entity.ApplicationsEntity;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.entity.JobStatusRecordEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.constant.CommonConstants;
import com.item.framework.constant.InterviewMailStatusEnum;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.service.ApplicationInterviewReportsService;
import com.item.service.ApplicationScreeningReportsService;
import com.item.service.ApplicationStageLogService;
import com.item.service.ApplicationsService;
import com.item.service.ApplicationsSyncService;
import com.item.service.CandidateJobService;
import com.item.service.CandidateService;
import com.item.service.JobService;
import com.item.service.JobStatusRecordService;
import com.item.service.migration.DataMigrationMappingService;
import com.item.vo.ApplicationsSyncVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Applications同步服务实现类
 * 
 * 处理PostgreSQL candidates.applications表到MySQL r_candidate_job表的数据同步，
 * 提供单条记录同步功能，通过CandidateJobService更新MySQL数据，
 * 全量同步功能由定时任务调用，添加错误处理和日志记录。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ApplicationsSyncServiceImpl implements ApplicationsSyncService {

    private final CandidateJobService candidateJobService;
    private final DataMigrationMappingService dataMigrationMappingService;
    private final JobService jobService;
    private final JobEsService jobEsService;
    private final CandidateService candidateService;
    private final ApplicationScreeningReportsService  applicationScreeningReportsService;
    private final ApplicationInterviewReportsService applicationInterviewReportsService;
    private final ApplicationStageLogService applicationStageLogService;
    private final JobStatusRecordService jobStatusRecordService;
    private final ApplicationsService applicationsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncSingleApplication(ApplicationsEntity application) {
        log.info("Starting sync for single application: {}", application.getId());
        try {
            //校验
            Optional<DataMigrationMappingEntity> jobOptional = dataMigrationMappingService.findByPgsqlIdAndType(String.valueOf(application.getJobId()), MigrationBusTypeEnum.JOB);
            if (jobOptional.isEmpty()){
                return false;
            }
            //JobEntity jobEntity = jobService.getById(jobOptional.get().getMysqlId());
            JobEsEntity jobEsEntity = jobEsService.getJobById(jobOptional.get().getMysqlId());
            if (jobEsEntity==null){
                return false;
            }
            Optional<DataMigrationMappingEntity> candidateOptional = dataMigrationMappingService.findByPgsqlIdAndType(application.getCandidateId(), MigrationBusTypeEnum.CANDIDATE);
            if (candidateOptional.isEmpty()){
                return false;
            }
            CandidateEntity candidateEntity = candidateService.getById(candidateOptional.get().getMysqlId());
            if (candidateEntity==null){
                return false;
            }
            boolean success= syncSingleApplicationInternal(application,jobEsEntity,candidateEntity);
            log.info("sync for single application End: {}", application.getId());
            return  success;
        } catch (Exception e) {
            log.error("Failed to sync application: {}, error: {}", application.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 同步单个Application记录（内部实现）
     * 
     * @param application PostgreSQL application记录
     * @return 是否同步成功
     */
    private boolean syncSingleApplicationInternal(ApplicationsEntity application,JobEsEntity jobEsEntity,CandidateEntity candidateEntity) {
        try {
            ApplicationScreeningReportsEntity screeningReports = applicationScreeningReportsService.getReportByApplicationId(UUID.fromString(application.getId()));
            ApplicationInterviewReportsEntity interviewReports = applicationInterviewReportsService.getByApplicationId(UUID.fromString(application.getId()));
            LocalDateTime screeningDate=null;
            LocalDateTime interviewDate=null;
            if (screeningReports!=null && screeningReports.getScreeningDate()!=null){
                screeningDate=screeningReports.getScreeningDate().toLocalDateTime();
            }
            if (interviewReports!=null && interviewReports.getCreatedOn()!=null){
                interviewDate=interviewReports.getCreatedOn().toLocalDateTime();
            }
            // 1 关联表数据
            //判断关联表是否存在
            Optional<DataMigrationMappingEntity> mappingOptional = dataMigrationMappingService.findByPgsqlIdAndType(application.getId(), MigrationBusTypeEnum.CANDIDATE_JOB);
            CandidateJobEntity candidateJob=new CandidateJobEntity();
            if (mappingOptional.isPresent()){
                CandidateJobEntity oldCandidateJob=candidateJobService.getById(mappingOptional.get().getMysqlId());
                if (oldCandidateJob!=null){
                    candidateJob=oldCandidateJob;
                    //删除流转记录
                    jobStatusRecordService.deleteByCandidateJobId(oldCandidateJob.getId());
                }
            }
            candidateJob.setCandidateId(candidateEntity.getId());
            candidateJob.setJobId(jobEsEntity.getId());
            candidateJob.setCustomerId(jobEsEntity.getCustomerId());
            candidateJob.setCompanyCode(jobEsEntity.getCompanyCode());
            // 状态映射：PostgreSQL status -> MySQL apply_status
            int mysqlStatus = mapPostgresStatusToMysql(application.getStatus(),screeningReports,interviewReports);
            candidateJob.setApplyStatus(mysqlStatus);
            candidateJob.setCreateTime(application.getCreatedOn().toLocalDateTime());
            candidateJob.setUpdateTime(LocalDateTime.now());
            List<LocationValRecordDTO> locations = jobEsEntity.getLocations();
            if(CollectionUtils.isNotEmpty(locations)){
                candidateJob.setJobCountryId(locations.getFirst().getCountryId());
                candidateJob.setJobCountryName(locations.getFirst().getCountryName());
                candidateJob.setJobStateId(locations.getFirst().getStateId());
                candidateJob.setJobStateName(locations.getFirst().getStateName());
                candidateJob.setJobCityId(locations.getFirst().getCityId());
                candidateJob.setJobCityName(locations.getFirst().getCityName());
            }
            List<ApplicationStageLogEntity> stageLogs=applicationStageLogService.listByApplicationId(UUID.fromString(application.getId()));
            if (CollectionUtils.isNotEmpty(stageLogs)){
                //发送邮件时间
                for (ApplicationStageLogEntity  stageLogEntity : stageLogs) {
                    //发送面试邮件
                    if (stageLogEntity.getId()==21 || stageLogEntity.getId()==2 || stageLogEntity.getId()==12){
                        candidateJob.setInterviewMailStatus(InterviewMailStatusEnum.SEND.getCode());
                        candidateJob.setInterviewStartTime(stageLogEntity.getUpdatedOn().toLocalDateTime());
                        break;
                    }
                }
            }

            candidateJobService.saveOrUpdate(candidateJob);
            //2 流转记录
            List<JobStatusRecordEntity> recordEntityList=convertStatusResord(candidateJob,screeningDate,interviewDate, stageLogs);
            jobStatusRecordService.saveBatch(recordEntityList);
            //3 迁移记录
            dataMigrationMappingService.saveMapping(application.getId(), candidateJob.getId(), MigrationBusTypeEnum.CANDIDATE_JOB);
            return true;
        } catch (Exception e) {
            log.error("Failed to sync single application: {}", application.getId(), e);
            return false;
        }
    }

    /**
     * 将PostgreSQL状态映射为MySQL状态码
     * 
     * @param postgresStatus PostgreSQL状态字符串
     * @return MySQL状态码
     */
    private int mapPostgresStatusToMysql(String postgresStatus,ApplicationScreeningReportsEntity screeningReports,
                                         ApplicationInterviewReportsEntity interviewReports) {
        if ("REJECTED".equals(postgresStatus)) {
            return JobApplyStatus.DENIED.getCode();
        }
        if ("ACCEPTED".equals(postgresStatus)) {
            return JobApplyStatus.READY.getCode();
        }
        if (interviewReports!=null){
            return JobApplyStatus.VETTED.getCode();
        }
        if (screeningReports!=null){
            return JobApplyStatus.SCREENED.getCode();
        }
        return JobApplyStatus.SUBMITTED.getCode();
    }

    /**
     * 转换流转记录
     * @param candidateJob
     * @param screeningDate
     * @param interviewDate
     * @param stageLogs
     * @return
     */
    private List<JobStatusRecordEntity> convertStatusResord(CandidateJobEntity candidateJob,LocalDateTime screeningDate,
                                                            LocalDateTime interviewDate,List<ApplicationStageLogEntity> stageLogs) {
        List<JobStatusRecordEntity> recordEntityList=new ArrayList<>();
        if (candidateJob.getApplyStatus()>=JobApplyStatus.SUBMITTED.getCode()){
            JobStatusRecordEntity  jobStatusRecordEntity=new JobStatusRecordEntity();
            jobStatusRecordEntity.setCandidateJobId(candidateJob.getId());
            jobStatusRecordEntity.setCompanyCode(candidateJob.getCompanyCode());
            jobStatusRecordEntity.setApplyStatus(JobApplyStatus.SUBMITTED.getCode());
            jobStatusRecordEntity.setCreateTime(candidateJob.getCreateTime());
            jobStatusRecordEntity.setUpdateTime(LocalDateTime.now());
            jobStatusRecordEntity.setApplyEvent(JobApplyStatusEvent.SUBMIT.toString());
            recordEntityList.add(jobStatusRecordEntity);
        }
        if (screeningDate!=null){
            JobStatusRecordEntity  jobStatusRecordEntity=new JobStatusRecordEntity();
            jobStatusRecordEntity.setCandidateJobId(candidateJob.getId());
            jobStatusRecordEntity.setCompanyCode(candidateJob.getCompanyCode());
            jobStatusRecordEntity.setOldApplyStatus(JobApplyStatus.SUBMITTED.getCode());
            jobStatusRecordEntity.setApplyStatus(JobApplyStatus.SCREENED.getCode());
            jobStatusRecordEntity.setCreateTime(screeningDate);
            jobStatusRecordEntity.setUpdateTime(LocalDateTime.now());
            jobStatusRecordEntity.setApplyEvent(JobApplyStatusEvent.SCREEN.toString());
            recordEntityList.add(jobStatusRecordEntity);
        }
        if (interviewDate!=null){
            JobStatusRecordEntity  jobStatusRecordEntity=new JobStatusRecordEntity();
            jobStatusRecordEntity.setCandidateJobId(candidateJob.getId());
            jobStatusRecordEntity.setCompanyCode(candidateJob.getCompanyCode());
            jobStatusRecordEntity.setOldApplyStatus(JobApplyStatus.SCREENED.getCode());
            jobStatusRecordEntity.setApplyStatus(JobApplyStatus.VETTED.getCode());
            jobStatusRecordEntity.setCreateTime(interviewDate);
            jobStatusRecordEntity.setUpdateTime(LocalDateTime.now());
            jobStatusRecordEntity.setApplyEvent(JobApplyStatusEvent.VETTED.toString());
            recordEntityList.add(jobStatusRecordEntity);
        }
        if (candidateJob.getApplyStatus()==JobApplyStatus.READY.getCode()){
            JobStatusRecordEntity  review=new JobStatusRecordEntity();
            review.setCandidateJobId(candidateJob.getId());
            review.setCompanyCode(candidateJob.getCompanyCode());
            review.setOldApplyStatus(JobApplyStatus.VETTED.getCode());
            review.setApplyStatus(JobApplyStatus.REVIEW.getCode());
            review.setCreateTime(getReadyDate(stageLogs));
            review.setUpdateTime(LocalDateTime.now());
            review.setApplyEvent(JobApplyStatusEvent.AI_PASS.toString());
            recordEntityList.add(review);

            JobStatusRecordEntity  jobStatusRecordEntity=new JobStatusRecordEntity();
            jobStatusRecordEntity.setCandidateJobId(candidateJob.getId());
            jobStatusRecordEntity.setCompanyCode(candidateJob.getCompanyCode());
            jobStatusRecordEntity.setOldApplyStatus(JobApplyStatus.REVIEW.getCode());
            jobStatusRecordEntity.setApplyStatus(JobApplyStatus.READY.getCode());
            jobStatusRecordEntity.setCreateTime(getReadyDate(stageLogs));
            jobStatusRecordEntity.setUpdateTime(LocalDateTime.now());
            jobStatusRecordEntity.setApplyEvent(JobApplyStatusEvent.REVIEW_PASS.toString());
            recordEntityList.add(jobStatusRecordEntity);
        }
        if (candidateJob.getApplyStatus()==JobApplyStatus.DENIED.getCode()){
            JobStatusRecordEntity  jobStatusRecordEntity=new JobStatusRecordEntity();
            jobStatusRecordEntity.setCandidateJobId(candidateJob.getId());
            jobStatusRecordEntity.setCompanyCode(candidateJob.getCompanyCode());
            jobStatusRecordEntity.setOldApplyStatus(JobApplyStatus.READY.getCode());
            jobStatusRecordEntity.setApplyStatus(JobApplyStatus.DENIED.getCode());
            jobStatusRecordEntity.setCreateTime(getDeniedDate(stageLogs));
            jobStatusRecordEntity.setUpdateTime(LocalDateTime.now());
            jobStatusRecordEntity.setApplyEvent(JobApplyStatusEvent.REJECT.toString());
            recordEntityList.add(jobStatusRecordEntity);
        }
        return recordEntityList;
    }

//    /**
//     * 获取人工审核时间
//     * @param stageLogs
//     * @return
//     */
//    private LocalDateTime getReviewDate(List<ApplicationStageLogEntity> stageLogs){
//        if (CollectionUtils.isNotEmpty(stageLogs)){
//            for (ApplicationStageLogEntity logEntity:stageLogs){
//                if (logEntity.getStageId().intValue()==21 || logEntity.getStageId().intValue()==25){
//                    return logEntity.getUpdatedOn().toLocalDateTime();
//                }
//            }
//        }
//        return null;
//    }

    /**
     * 获取就绪时间
     * @param stageLogs
     * @return
     */
    private LocalDateTime getReadyDate(List<ApplicationStageLogEntity> stageLogs){
        if (CollectionUtils.isNotEmpty(stageLogs)){
            for (ApplicationStageLogEntity logEntity:stageLogs){
                if (logEntity.getStageId().intValue()==3){
                    return logEntity.getUpdatedOn().toLocalDateTime();
                }
            }
        }
        return null;
    }

    /**
     * 获取拒绝时间
     * @param stageLogs
     * @return
     */
    private LocalDateTime getDeniedDate(List<ApplicationStageLogEntity> stageLogs){
        if (CollectionUtils.isNotEmpty(stageLogs)){
            for (ApplicationStageLogEntity logEntity:stageLogs){
                if (logEntity.getStageId().intValue()==11 || logEntity.getStageId().intValue()==22 || logEntity.getStageId().intValue()==23){
                    return logEntity.getUpdatedOn().toLocalDateTime();
                }
            }
        }
        return null;
    }

    /**
     * 批量同步
     * @param syncVo
     * @return
     */
    @Override
    public boolean applicationSync(ApplicationsSyncVO syncVo) {
        List<ApplicationsEntity> applicationsEntities = applicationsService.listBySyncVo(syncVo);
        if (CollectionUtils.isNotEmpty(applicationsEntities)){
            log.info("applicationSync size: {}", applicationsEntities.size());
            for (ApplicationsEntity application:applicationsEntities){
                syncSingleApplication(application);
            }
        }
        return true;
    }

    /**
     * 未同步的
     * @return
     */
    @Override
    public List<String> getNotApplicationSync() {
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
        return  applicationIds;
    }
}
