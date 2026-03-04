package com.item.service;

import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.dto.job.JobCreateBO;
import com.item.vo.ai.InterviewResultVO;

/**
 * Service for handling job post-processing operations
 * such as AyrShare posting and AI interview creation
 *
 * @author system
 * @since 1.0.0
 */
public interface JobPostProcessingService {
    
    /**
     * Post job to AyrShare platform
     *
     * @param jobCreateBO Job creation business object
     * @param companyInfo Company information
     * @param jobId Job ID
     */
    void ayrSharePost(JobCreateBO jobCreateBO, IamCompanyDetailDTO companyInfo, Long jobId);
    
    /**
     * Create AI interview for the job
     *
     * @param jobCreateBO Job creation business object
     * @return Interview result with URL ID
     */
    InterviewResultVO createAIInterview(JobCreateBO jobCreateBO);
}
