package com.item.service;

import com.item.dto.naukri.NaukriApplicationDTO;

import java.util.List;
import java.util.Map;

/**
 * Naukri Applications Integration.
 */
public interface NaukriApplicationsService {

    /**
     * Fetch applications for a Naukri job with pagination.
     *
     * @param naukriJobId Naukri job ID
     * @param page        zero-based page index from Amplify API
     * @return list of applications on the requested page
     */
    List<NaukriApplicationDTO> fetchApplicationsForJob(String naukriJobId, int page);

    /**
     * Get details for a specific application.
     */
    Map<String, Object> getApplicationDetails(String applicationId);

    /**
     * Get a temporary URL to download applicant's resume.
     */
    String getApplicantResumeUrl(String applicationId);

    /**
     * Update application stage/status on Naukri side.
     */
    boolean updateApplicationStage(String applicationId, String stage);
}


