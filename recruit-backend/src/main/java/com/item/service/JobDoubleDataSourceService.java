package com.item.service;

import com.item.dto.job.JobAuditHistoryBO;
import com.item.dto.job.JobCreateBO;
import com.item.dto.job.JobUpdateBO;

public interface JobDoubleDataSourceService {
    Long publishJob(JobCreateBO dto);

    /**
     * Update job info in DB and ES by jobId
     */
    boolean updateJob(JobUpdateBO bo);

    /**
     * 更新ayrshare状态
     *
     * @param jobId
     * @param ayrshareStatus
     * @return
     */
    boolean updateJobAyrShareStatus(Long jobId, Integer ayrshareStatus);

    boolean updateJobStatus(Long id, Integer status, JobAuditHistoryBO bo);

    /**
     * Delete job from both DB and ES by jobId
     * 
     * @param jobId the job ID to delete
     * @return true if deletion successful, false otherwise
     */
    boolean deleteJob(Long jobId);
}
