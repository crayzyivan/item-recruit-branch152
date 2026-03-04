package com.item.service;

import com.item.vo.RecommendCandidateCountVO;
import com.item.vo.RecommendCandidateVO;

import java.util.List;

/**
 * 职位推荐候选人服务接口
 * 
 * @author system
 * @since 2025-10-23
 */
public interface JobRecommendService {
    
    /**
     * 根据职位ID推荐候选人
     * 
     * @param jobId 职位ID
     * @return 推荐的候选人列表
     */
    List<RecommendCandidateVO> recommendCandidates(Long jobId);

    /**
     * 根据职位ids获取推荐候选人的人数
     *
     * @param jobIds
     * @return
     */
    List<RecommendCandidateCountVO> recommendCandidateCount(List<Long> jobIds);
}
