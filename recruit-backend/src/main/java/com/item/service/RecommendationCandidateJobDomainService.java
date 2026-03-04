package com.item.service;

import com.item.vo.RecommendJobEmailRequestDTO;

/**
 * Recommendation candidate job domain service
 * Handles business logic for sending job recommendation emails
 *
 * @author hua.liu
 * @since 2025-10-23
 */
public interface RecommendationCandidateJobDomainService {

    /**
     * Send recommendation email to a single candidate
     *
     * @param request
     * @return true if sent successfully, false otherwise
     */
    Boolean sendRecommendationEmail(RecommendJobEmailRequestDTO request);
}

