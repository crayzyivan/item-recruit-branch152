package com.item.service;

import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.dto.job.JobCreateBO;
import com.item.dto.job.JobUpdateBO;

/**
 * Naukri posting integration via Zwayam Amplify.
 */
public interface NaukriService {

    /**
     * Whether this job should be posted to Naukri (India or Saudi Arabia),
     * and feature toggle is enabled.
     */
    boolean shouldPostToNaukri(JobCreateBO jobCreateBO);

    /**
     * Asynchronously post a job to Naukri when published.
     */
    void asyncPostJob(JobCreateBO jobCreateBO, String companyName, Long jobId);

    /**
     * Asynchronously update a job on Naukri when edited.
     */
    void asyncUpdateJob(JobUpdateBO jobUpdateBO);

    /**
     * Asynchronously unpublish a job from Naukri when closed/hidden.
     */
    void asyncUnpublishJob(String jobId);

    /**
     * Asynchronously refresh a job on Naukri.
     */
    void asyncRefreshJob(String jobId);
}


