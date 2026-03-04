package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.SaveRecommendationDTO;
import com.item.entity.RecommendationCandidateJobEntity;

import java.util.List;

/**
 * Recommendation candidate job service
 * Provides business operations for recommendation records
 *
 * @author hua.liu
 * @since 2025-10-23
 */
public interface RecommendationCandidateJobService extends IService<RecommendationCandidateJobEntity> {

    /**
     * Save a single recommendation record
     *
     * @param saveDTO save recommendation DTO
     * @return saved entity
     */
    RecommendationCandidateJobEntity saveRecommendation(SaveRecommendationDTO saveDTO);

    /**
     * Query recommendation records by job IDs
     *
     * @param jobIds job ID list
     * @return recommendation record list
     */
    List<RecommendationCandidateJobEntity> listByJobIds(List<Long> jobIds);

    /**
     * Query recommendation records by candidate IDs
     *
     * @param candidateIds candidate ID list
     * @return recommendation record list
     */
    List<RecommendationCandidateJobEntity> listByCandidateIds(List<Long> candidateIds);

    /**
     * Check if a recommendation exists for the given job and candidate
     *
     * @param jobId job ID
     * @param candidateId candidate ID
     * @return true if exists, false otherwise
     */
    boolean checkRecommendationExists(Long jobId, Long candidateId);
}

