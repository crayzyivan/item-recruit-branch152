package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.item.convert.CandidateJobConvert;
import com.item.dto.CandidateJobCountDTO;
import com.item.dto.CityDTO;
import com.item.dto.CountryDTO;
import com.item.dto.DictionaryDTO;
import com.item.dto.FullLocationDTO;
import com.item.dto.StateDTO;
import com.item.dto.report.CandidateSimpleDTO;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.framework.constant.*;
import com.item.framework.http.Pager;
import com.item.mapper.CandidateJobMapper;
import com.item.service.CandidateJobService;
import com.item.service.CandidateService;
import com.item.service.DictionaryService;
import com.item.service.JobService;
import com.item.service.LocationService;
import com.item.service.PointService;
import com.item.framework.error.BusinessException;
import com.item.vo.ApplicationUserInfoVO;
import com.item.vo.CandidateJobQueryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应聘者职位关系服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateJobServiceImpl extends ServiceImpl<CandidateJobMapper, CandidateJobEntity> implements CandidateJobService {
    
    /**
     * Batch processing size limit
     */
    private static final int BATCH_SIZE = 200;

    /**
     * Database field names extracted from entity using lambda method references
     */
    private static final String FIELD_JOB_ID = "job_id";
    private static final String FIELD_COUNT = "count";
    private static final String COUNT_ALIAS = "count(*) as count";


    private final JobService jobService;
    private final CandidateService candidateService;
    private final DictionaryService dictionaryService;
    private final LocationService locationService;
    private final PointService pointService;

    @Override
    public boolean applyJob(CandidateJobEntity dto) {
        return save(dto);
    }

    @Override
    public List<CandidateJobEntity> getByCandidateIdAndJobId(Long candidateId, Long jobId) {
        return lambdaQuery()
                .eq(CandidateJobEntity::getCandidateId, candidateId)
                .eq(CandidateJobEntity::getJobId, jobId)
                .list();
    }

    @Override
    public List<CandidateJobEntity> listByJobIds(Set<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return Lists.newArrayList();
        }
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CandidateJobEntity::getJobId, jobIds);
        queryWrapper.select(CandidateJobEntity::getJobId, CandidateJobEntity::getCandidateId);
        return this.list(queryWrapper);
    }

    @Override
    public List<CandidateJobEntity> listTimeByJobIds(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CandidateJobEntity::getId, ids);
        queryWrapper.select(CandidateJobEntity::getId, CandidateJobEntity::getCreateTime, CandidateJobEntity::getUpdateTime, CandidateJobEntity::getJobId, CandidateJobEntity::getCandidateId, CandidateJobEntity::getPlaceId);
        return this.list(queryWrapper);
    }

    @Override
    public CandidateJobEntity getLatestByCandidateIdAndJobId(Long candidateId, Long jobId) {
        return lambdaQuery()
                .eq(CandidateJobEntity::getCandidateId, candidateId)
                .eq(CandidateJobEntity::getJobId, jobId)
                .orderByDesc(CandidateJobEntity::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public boolean updateApplyStatus(Long id, Integer status, String reason) {
        CandidateJobEntity entity = new CandidateJobEntity();
        entity.setId(id);
        entity.setApplyStatus(status);
        // 只有当原因不为空时才更新原因字段
        if (StringUtils.isNotBlank(reason)) {
            entity.setReason(reason);
        }
        return updateById(entity);
    }

    @Override
    public Long applicationCount(Long jobId) {
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateJobEntity::getJobId, jobId);

        return this.count(queryWrapper);
    }

    @Override
    public Map<Long, Long> batchApplicationCount(Set<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return Collections.emptyMap();
        }

        Map<Long, Long> resultMap = Maps.newHashMap();

        // Check if need to split into batches
        if (jobIds.size() > BATCH_SIZE) {
            log.info("Large batch detected: {} jobIds, splitting into batches of {}", jobIds.size(), BATCH_SIZE);
            // Convert Set to List and partition using Guava
            List<Long> jobIdList = Lists.newArrayList(jobIds);
            List<List<Long>> batches = Lists.partition(jobIdList, BATCH_SIZE);
            
            // Process each batch
            for (List<Long> batchJobIds : batches) {
                Map<Long, Long> batchResults = processBatch(batchJobIds);
                resultMap.putAll(batchResults);
            }

        } else {
            // Process single batch
            resultMap = processBatch(Lists.newArrayList(jobIds));
        }

        // Ensure all jobIds are included in results (with count=0 if not found)
        Map<Long, Long> finalResults = Maps.newHashMapWithExpectedSize(jobIds.size());
        for (Long jobId : jobIds) {
            finalResults.put(jobId, resultMap.getOrDefault(jobId, 0L));
        }
        return finalResults;
    }

    /**
     * Process a single batch of jobIds
     * @param jobIds batch of job IDs to process
     * @return map of jobId to application count
     */
    private Map<Long, Long> processBatch(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return Collections.emptyMap();
        }

        // Use MyBatis Plus to query with group by
        QueryWrapper<CandidateJobEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(FIELD_JOB_ID, COUNT_ALIAS)
                .in(FIELD_JOB_ID, jobIds)
                .groupBy(FIELD_JOB_ID);

        List<Map<String, Object>> mapResults = this.listMaps(queryWrapper);
        
        // Convert Map results to result map
        Map<Long, Long> results = Maps.newHashMapWithExpectedSize(mapResults.size());
        for (Map<String, Object> map : mapResults) {
            Long jobId = Long.valueOf(map.get(FIELD_JOB_ID).toString());
            Long count = Long.valueOf(map.get(FIELD_COUNT).toString());
            results.put(jobId, count);
        }
        
        return results;
    }

    @Override
    public CandidateJobCountDTO getCandidateNumber(Long jobId) {
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateJobEntity::getJobId, jobId);
        List<CandidateJobEntity> list = this.list(queryWrapper);
        Map<Integer, List<CandidateJobEntity>> groupMap = list.stream()
                .collect(Collectors.groupingBy(CandidateJobEntity::getApplyStatus));
        CandidateJobCountDTO candidateJobCountDTO = new CandidateJobCountDTO();
        candidateJobCountDTO.setApplications(list.size());
        groupMap.forEach((applyStatus, jobList) -> {
            if (applyStatus == JobApplyStatus.VETTED.getCode()) {
                candidateJobCountDTO.setAIVetted(jobList.size());
            } else if (applyStatus == JobApplyStatus.SCREENED.getCode()) {
                candidateJobCountDTO.setScreened(jobList.size());
            } else if (applyStatus == JobApplyStatus.REVIEW.getCode()) {
                candidateJobCountDTO.setPendingReview(jobList.size());
            } else if (applyStatus == JobApplyStatus.READY.getCode()) {
                candidateJobCountDTO.setReady(jobList.size());
            } else if (applyStatus == JobApplyStatus.BACKGROUND.getCode()) {
                candidateJobCountDTO.setBackgroundChecked(jobList.size());
            } else if (applyStatus == JobApplyStatus.DENIED.getCode()) {
                candidateJobCountDTO.setDenied(jobList.size());
            }else if (applyStatus == JobApplyStatus.MANUAL_REVIEW.getCode()) {
                candidateJobCountDTO.setManualReview(jobList.size());
            }
        });
        return candidateJobCountDTO;
    }

    /**
     * 分页获取应聘者职位关系列表
     * @param queryVO
     * @return
     */
    @Override
    public Page<CandidateJobEntity> selectCandidateJobPageList(CandidateJobQueryVO queryVO) {
        Page<CandidateJobEntity> page = new Page<>(queryVO.getPageIndex(), queryVO.getPageSize());
        //1 查询符合要求的候选人与职位表记录
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new QueryWrapper<CandidateJobEntity>().lambda();
        if(Objects.nonNull(queryVO.getJobId())) {
            queryWrapper.eq(CandidateJobEntity::getJobId,queryVO.getJobId());
        }
        if (Objects.nonNull(queryVO.getApplyStatus())){
            queryWrapper.eq(CandidateJobEntity::getApplyStatus, queryVO.getApplyStatus());
        }
        if (Objects.nonNull(queryVO.getScore())) {
            queryWrapper.and(wrapper -> wrapper
                    .ge(CandidateJobEntity::getAssessmentScore, queryVO.getScore())
                    .or()
                    .ge(CandidateJobEntity::getOverallScore, queryVO.getScore())
            );
        }
        queryWrapper.orderByDesc(CandidateJobEntity::getCreateTime);
        page(page, queryWrapper);
        return page;
    }

    @Override
    public List<CandidateJobEntity> checkInfo(String candidateEmail, String interviewId, Long applicationId, String candidateName) {
        return baseMapper.getCandidateJobEntityByInterviewInfo(candidateEmail, interviewId, JobApplyStatus.VETTED.getCode(), applicationId, candidateName);
    }

    @Override
    public Pager<CandidateSimpleDTO> getCandidateJobByCompanyCode(CandidateJobQueryVO query) {
        Page<CandidateJobEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(CandidateJobEntity::getCompanyCode, query.getCompanyCode())
                .gt(CandidateJobEntity::getApplyStatus, JobApplyStatus.SUBMITTED.getCode())
                .orderByDesc(CandidateJobEntity::getId);

        this.page(page, queryWrapper);

        var pagerDTO = Pager.build(page, CandidateJobConvert.INSTANCE::toSimpleDTO);
        List<Long> jobIds = pagerDTO.getCurrentPageRecords().stream().map(CandidateSimpleDTO::getJobId).toList();
        Map<Long,JobEntity> jobMap =
                jobService.getJobsByIds(jobIds).stream().collect(Collectors.toMap(JobEntity::getId,
                job -> job));

        List<Long> candidateIds =
                pagerDTO.getCurrentPageRecords().stream().map(CandidateSimpleDTO::getCandidateId).toList();
        Map<Long, CandidateEntity> candidateMap =
                candidateService.getByIds(candidateIds).stream().collect(Collectors.toMap(CandidateEntity::getId,
                 candidateEntity -> candidateEntity));
        List<Long> cityIds = new ArrayList<>();
        List<Long> stateIds = new ArrayList<>();
        List<Long> countryIds = new ArrayList<>();
        candidateMap.values().forEach(candidate -> {
            cityIds.add(candidate.getCityId());
            stateIds.add(candidate.getStateId());
            countryIds.add(candidate.getCountryId());
        });

        Map<Long, CityDTO> cityMap =
                locationService.listByCityIds(cityIds).stream().collect(Collectors.toMap(CityDTO::getId,
                c -> c));
        Map<Long, StateDTO> stateMap =
                locationService.listByStateIds(stateIds).stream().collect(Collectors.toMap(StateDTO::getId, s -> s));
        Map<Long, CountryDTO> countryMap =
                locationService.listByCountryIds(countryIds).stream().collect(Collectors.toMap(CountryDTO::getId,
                        c -> c));

        Map<Long, String> salaryDict = dictionaryService
                .listByType(DictTypeConstans.SALARYTYPE)
                .stream()
                .collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue));
        Map<Long, String> currencyDict = dictionaryService
                .listByType(DictTypeConstans.CURRENCYTYPE)
                .stream()
                .collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue));

        pagerDTO.getCurrentPageRecords().forEach(record -> {
            Long jobId = record.getJobId();

            JobEntity jobEntity = jobMap.get(jobId);
            CandidateEntity candidateEntity = candidateMap.get(record.getCandidateId());



            if (jobEntity != null) {
                record.setJobTitle(jobEntity.getTitle());
                record.setJobLocation(jobEntity.getLocationName());
            } else {
                record.setJobTitle(CommonConstants.StrConstants.JOB_BY_DELETED);
            }
            if (candidateEntity != null) {
                FullLocationDTO fullLocationDTO = new FullLocationDTO();
                CityDTO cityDTO = cityMap.get(candidateEntity.getCityId());
                if (cityDTO != null) {
                    fullLocationDTO.setCityName(cityDTO.getName());
                }
                StateDTO stateDTO = stateMap.get(candidateEntity.getStateId());
                if (stateDTO != null) {
                    fullLocationDTO.setStateName(stateDTO.getName());
                }
                CountryDTO countryDTO = countryMap.get(candidateEntity.getCountryId());
                if (countryDTO != null) {
                    fullLocationDTO.setCountryName(countryDTO.getName());
                }
                record.setCandidateEmail(candidateEntity.getCandidateEmail());
                record.setFullLocation(fullLocationDTO);
                record.setCandidateName(candidateEntity.getCandidateName());
                record.setExpectedSalary(candidateEntity.getExpectedSalary().intValue());
                record.setSalaryTypeId(candidateEntity.getSalaryTypeId());
                record.setSalaryTypeName(salaryDict.get(candidateEntity.getSalaryTypeId()));
                record.setCurrencyName(currencyDict.get(candidateEntity.getCurrencyTypeId()));
                record.setApplyStatusName(JobApplyStatus.fromCode(record.getApplyStatus()).getName());
                record.setApplyStatus(record.getApplyStatus());
            }
        });

        return pagerDTO;
    }

    /**
     * 查询没有ai简历评分的记录
     * @return
     */
    @Override
    public List<CandidateJobEntity> listByAssessmentScoreIsNull() {
        LambdaQueryWrapper<CandidateJobEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.isNull(CandidateJobEntity::getAssessmentScore);
        return this.list(wrapper);
    }

    /**
     * 查询没有发送面试链接的记录
     * @return
     */
    @Override
    public List<CandidateJobEntity> listNotSentInterviewMailCandidates(LocalDateTime cutoffTime) {
        LambdaQueryWrapper<CandidateJobEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(CandidateJobEntity::getInterviewMailStatus, InterviewMailStatusEnum.NOTSEND.getCode());
        wrapper.ge(CandidateJobEntity::getCreateTime, cutoffTime);
        return this.list(wrapper);
    }

    @Override
    public List<CandidateJobEntity> listNotBookInterviewPhoneCandidates() {
        LambdaQueryWrapper<CandidateJobEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(CandidateJobEntity::getInterviewPhoneStatus, InterviewPhoneStatusEnum.NOT_SCHEDULED.getCode());
        wrapper.ge(CandidateJobEntity::getAssessmentScore, CommonConstants.MIN_INTERVIEW_MAIL_SCORE);
        return this.list(wrapper);
    }

    @Override
    public Pager<CandidateSimpleDTO> getApplicationsCandidateByCompanyCode(CandidateJobQueryVO query) {
        Page<CandidateEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());
        Page<CandidateEntity> candidateEntityPage = baseMapper.selectCandidatePage(page, query.getCompanyCode());
        Map<Long, CandidateEntity> candidateMap =
                candidateEntityPage.getRecords().stream().collect(Collectors.toMap(CandidateEntity::getId,
                        candidateEntity -> candidateEntity));
        var pagerDTO = Pager.build(candidateEntityPage, CandidateJobConvert.INSTANCE::candidateEntitiesToSimpleDTOs);
        pagerDTO.getCurrentPageRecords().forEach(record -> {
            CandidateEntity candidateEntity = candidateMap.get(record.getCandidateId());
            if (candidateEntity != null) {
                FullLocationDTO fullLocationDTO = new FullLocationDTO();
                fullLocationDTO.setCityName(candidateEntity.getCityName());
                fullLocationDTO.setStateName(candidateEntity.getStateName());
                fullLocationDTO.setCountryName(candidateEntity.getCountryName());
                record.setCandidateEmail(candidateEntity.getCandidateEmail());
                record.setFullLocation(fullLocationDTO);
            }
        });
        return pagerDTO;

    }

    /**
     * 获取取消面试id
     * @return
     */
    @Override
    public List<Long> getCancelInterviewFreezeIds() {
        LambdaQueryWrapper<CandidateJobEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(CandidateJobEntity::getApplyStatus,JobApplyStatus.SCREENED.getCode());
        wrapper.eq(CandidateJobEntity::getInterviewMailStatus, InterviewMailStatusEnum.SEND.getCode());
        wrapper.lt(CandidateJobEntity::getInterviewEndTime, LocalDateTime.now());
        wrapper.select(CandidateJobEntity::getId);
        List<CandidateJobEntity> list = this.list(wrapper);
        if (CollectionUtils.isNotEmpty(list)){
            return list.stream().map(CandidateJobEntity::getId).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Check if interview email has been sent for a job
     * @param jobId job ID
     * @return true if interview email has been sent, false otherwise
     */
    @Override
    public Boolean hasInterviewMailSent(Long jobId) {
        if (jobId == null) {
            return false;
        }
        JobEntity job = jobService.getById(jobId);
        if (job == null) {
            return false;
        }
        if (pointService.isExempt(job.getCompanyCode())){
            return false;
        }
        LambdaQueryWrapper<CandidateJobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CandidateJobEntity::getJobId, jobId);
        wrapper.eq(CandidateJobEntity::getInterviewMailStatus, InterviewMailStatusEnum.SEND.getCode());
        wrapper.last("LIMIT 1");
        return this.count(wrapper) > 0;
    }

    /**
     * 获取过期的应聘者职位关系
     * @param cooldownCutoffTime
     * @return
     */
    @Override
    public List<CandidateJobEntity> findExpiredCandidateJobs(LocalDateTime cooldownCutoffTime) {
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                // 更新时间小于冷却期截止时间
                .lt(CandidateJobEntity::getUpdateTime, cooldownCutoffTime)
                // 申请状态不是拒绝状态
                .ne(CandidateJobEntity::getApplyStatus, JobApplyStatus.DENIED.getCode())
                // 逻辑删除标识为0（未删除）
                .eq(CandidateJobEntity::getDeleted, 0)
                // 按更新时间升序排列
                .orderByAsc(CandidateJobEntity::getUpdateTime);

        return this.list(queryWrapper);
    }

    /**
     * 根据申请ID获取用户信息（租户ID、用户ID、用户姓名）
     * @param applicationId 申请ID（r_candidate_job表的主键id）
     * @return 用户信息，包含租户ID、用户ID和用户姓名
     */
    @Override
    public ApplicationUserInfoVO getUserInfoByApplicationId(Long applicationId) {
        if (applicationId == null) {
            log.error("Application ID is null");
            throw new BusinessException(GlobalStatusCode.FAIL, "Application ID cannot be null");
        }

        // 1. 根据申请ID查询申请记录
        CandidateJobEntity candidateJobEntity = this.getById(applicationId);
        if (candidateJobEntity == null) {
            log.error("Candidate job entity not found for application ID: {}", applicationId);
            throw new BusinessException(GlobalStatusCode.FAIL, "Application not found");
        }

        // 2. 获取租户ID（公司代码）
        String tenantId = candidateJobEntity.getCompanyCode();
        if (StringUtils.isBlank(tenantId)) {
            log.info("Company code is blank for application ID: {}", applicationId);
        }

        // 3. 根据候选人ID查询候选人信息
        Long jobId = candidateJobEntity.getJobId();
        if (jobId == null) {
            log.error("job ID is null for application ID: {}", applicationId);
            throw new BusinessException(GlobalStatusCode.FAIL, "job ID not found");
        }
        JobEntity jobEntity = jobService.getJobsByIds(jobId);
        // 5. 构建返回对象
        ApplicationUserInfoVO userInfoVO = new ApplicationUserInfoVO();
        userInfoVO.setTenantId(tenantId);
        userInfoVO.setUserId(jobEntity.getCreateBy());
        userInfoVO.setUserName(jobEntity.getCreateUser());

        log.info("Retrieved user info for application ID {}: tenantId={}, userId={}, userName={}",
                applicationId, tenantId, jobEntity.getCreateBy(), jobEntity.getCreateUser());

        return userInfoVO;
    }

    @Override
    public boolean hasApplicationsByJobId(Long jobId) {
        if (jobId == null) {
            log.warn("Job ID is null when checking applications");
            return false;
        }
        
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateJobEntity::getJobId, jobId);
        queryWrapper.last("LIMIT 1");
        
        long count = this.count(queryWrapper);
        log.debug("Job {} has {} applications", jobId, count > 0 ? "existing" : "no");
        return count > 0;
    }

    @Override
    public CandidateJobEntity getCandidateJobById(Long id) {
        if  (id == null) {
            return null;
        }
        return this.getById(id);
    }

    @Override
    public void updateCandidateQuestionInfoById(Long id, String questionInfo) {
        if  (id == null) {
            return;
        }
        LambdaUpdateWrapper<CandidateJobEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CandidateJobEntity::getId, id);
        updateWrapper.set(CandidateJobEntity::getQuestionInfo, questionInfo);
        this.update(updateWrapper);
    }
}