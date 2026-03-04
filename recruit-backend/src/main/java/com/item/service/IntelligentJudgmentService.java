package com.item.service;

import com.item.es.entity.JobEsEntity;
import com.item.util.CandidateScores;

/**
 * 智能判断服务
 * 用于计算候选人的加权总分和阈值判断
 */
public interface IntelligentJudgmentService {
    
    /**
     * 计算候选人的加权总分
     * 
     * @param jobEsEntity 职位
     * @param candidateScores 候选人得分
     * @return 加权总分（四舍五入后的整数）
     */
    Integer calculateWeightedScore(JobEsEntity jobEsEntity, CandidateScores candidateScores);
    
    /**
     * 检查候选人分数是否通过阈值
     * 如果有 assessmentScore，则只比较 assessmentScore
     * 如果没有 assessmentScore，则依次比较其他分数，任何一个低于阈值则返回 false
     * 
     * @param jobId 职位ID
     * @param candidateScores 候选人得分
     * @return true表示通过，false表示不通过
     */
    boolean checkThresholdScore(Long jobId, CandidateScores candidateScores);
}
