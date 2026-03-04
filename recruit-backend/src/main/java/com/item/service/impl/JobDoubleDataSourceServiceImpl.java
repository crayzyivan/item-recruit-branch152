package com.item.service.impl;

import com.item.convert.CandidateJobConvert;
import com.item.convert.JobAuditHistoryConvert;
import com.item.convert.JobConvert;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.job.JobAuditHistoryBO;
import com.item.dto.job.JobCreateBO;
import com.item.dto.job.JobUpdateBO;
import com.item.entity.JobEntity;
import com.item.es.JobEsService;
import com.item.es.ResumeEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.constant.JobStatus;
import com.item.framework.constant.XmlFeedConstants;
import com.item.framework.constant.XmlFeedConstants.PlatformType;
import com.item.service.JobAuditHistoryService;
import com.item.service.JobDoubleDataSourceService;
import com.item.service.JobService;
import com.item.service.XmlFeedConfigService;
import com.item.util.UserContextUtil;
import com.item.vo.ai.JobMatchResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobDoubleDataSourceServiceImpl implements JobDoubleDataSourceService {
    private final JobService jobService;
    private final JobEsService jobEsService;
    private final JobConvert jobConvert;
    private final ResumeEsService resumeEsService;
    private final ThreadPoolTaskExecutor aiTaskExecutor;
    private final XmlFeedConfigService xmlFeedConfigService;
    private final JobAuditHistoryService jobAuditHistoryService;
    private final JobAuditHistoryConvert jobAuditHistoryConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishJob(JobCreateBO dto) {
        JobEntity jobEntity = jobService.createJob(dto);
        // 同步到ES
        JobEsEntity jobEsEntity = jobConvert.toJobEsFromDTO(dto);
        jobEsEntity.setDeleted(0);
        jobEsEntity.setId(jobEntity.getId());
        jobEsEntity.setCreateTime(LocalDateTime.now());
        jobEsEntity.setUpdateTime(LocalDateTime.now());
        // Ensure companyTitleHash is set in ES entity
        jobEsService.saveJobToEs(jobEsEntity);
        return jobEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJob(JobUpdateBO bo) {

        JobEntity jobEntity = jobConvert.convertFromUpdateBO(bo);

        if (!jobService.updateById(jobEntity)) {
            log.warn("update job failed {}", jobEntity);
            return false;
        }

        if(bo.getJobAuditHistory() != null) {
            var jobAuditHistoryEntity = jobAuditHistoryConvert.convertFromUpdateBO(bo.getJobAuditHistory());
            jobAuditHistoryService.save(jobAuditHistoryEntity);
        }

        // 2. 同步ES
        JobEsEntity jobEsEntity = new JobEsEntity();
        jobConvert.updateEsEntityFromUpdateBO(bo, jobEsEntity);
        jobEsEntity.setUpdateTime(LocalDateTime.now());
        jobEsService.updateJobEs(jobEsEntity);
        batchUpdateByJobId(jobEntity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJobAyrShareStatus(Long jobId, Integer ayrshareStatus) {
        JobEntity updateEntity = new JobEntity();
        updateEntity.setId(jobId);
        updateEntity.setAyrshareStatus(ayrshareStatus);

        boolean updateResult = jobService.updateById(updateEntity);
        // 2. 同步ES
        JobEsEntity jobEsEntity = new JobEsEntity();
        jobEsEntity.setId(jobId);
        jobEsEntity.setAyrshareStatus(ayrshareStatus);
        jobEsEntity.setUpdateTime(LocalDateTime.now());
        jobEsService.updateJobEs(jobEsEntity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJobStatus(Long id, Integer status, JobAuditHistoryBO bo) {
        JobEntity updateEntity = new JobEntity();
        updateEntity.setId(id);
        updateEntity.setJobStatus(status);

        boolean updateResult = jobService.updateById(updateEntity);
        if (!updateResult) {
            log.warn("Failed to update job status. JobId: {}, JobStatus: {}", id, status);
            return false;
        }

        var jobAuditHistoryEntity = jobAuditHistoryConvert.convertFromUpdateBO(bo);
        jobAuditHistoryService.save(jobAuditHistoryEntity);

        // 4. Update ES if exists
        JobEsEntity jobEsEntity = new JobEsEntity();
        jobEsEntity.setId(id);
        jobEsEntity.setJobStatus(status);
        jobEsEntity.setJobStatusName(JobStatus.getByCode(status).getDescription());
        jobEsEntity.setUpdateTime(java.time.LocalDateTime.now());
        jobEsService.updateJobEs(jobEsEntity);
        JobEntity jobEntity = jobService.getById(id);
        batchUpdateByJobId(jobEntity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(Long jobId) {
        // 1. Delete from database
        boolean dbDeleteResult = jobService.removeById(jobId);
        if (!dbDeleteResult) {
            log.warn("Failed to delete job from database. JobId: {}", jobId);
            return false;
        }

        // 2. Delete from ES
        JobEsEntity jobEsEntity = new JobEsEntity();
        jobEsEntity.setId(jobId);
        jobEsEntity.setDeleted(1);
        jobEsService.updateJobEs(jobEsEntity);
        log.info("Successfully deleted job from both DB and ES. JobId: {}", jobId);
        JobEntity jobEntity = new JobEntity();
        jobEntity.setId(jobId);
        jobEntity.setDeleted(true);
        batchUpdateByJobId(jobEntity);
        return true;
    }

    private void batchUpdateByJobId(JobEntity jobEntity){
        try {
            JobMatchResultVO jobMatchResultVO = new JobMatchResultVO();
            CandidateJobConvert.INSTANCE.jobEntityToJobMatchResultVO(jobEntity, jobMatchResultVO);
            jobMatchResultVO.setLocationId(null);
            jobMatchResultVO.setLocationName(null);
            resumeEsService.batchUpdateByJobId(jobMatchResultVO);
        } catch (Exception e) {
            log.info("deleted job from both DB and ES fila. JobId: {}", jobEntity.getId());
        }
    }
}
