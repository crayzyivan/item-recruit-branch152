package com.item.service.impl;

import com.item.dto.job.IntelligenceScoreRuleDTO;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.constant.JobIntelligenceStageEnum;
import com.item.framework.constant.JobIntelligenceSubStageEnum;
import com.item.service.IntelligentJudgmentService;
import com.item.util.CandidateScores;
import com.item.util.CandidateScoreWeights;
import com.item.util.ScoreCalculationUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 智能判断服务实现类
 * 用于计算候选人的加权总分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentJudgmentServiceImpl implements IntelligentJudgmentService {
    
    private final JobEsService jobEsService;
    
    /**
     * 计算候选人的加权总分
     * 
     * @param jobEsEntity 职位
     * @param candidateScores 候选人得分
     * @return 加权总分（四舍五入后的整数）
     */
    @Override
    public Integer calculateWeightedScore(JobEsEntity jobEsEntity, CandidateScores candidateScores) {

        // Get job information
        if (jobEsEntity == null) {
            return null;
        }
        log.info("Start calculating candidate weighted score, jobId: {}, candidateScores: {}", jobEsEntity.getId(), candidateScores);
        if (!jobEsEntity.getIntelligenceSwitch()) {
            log.warn("Job intelligence switch is closed, jobId: {}", jobEsEntity.getId());
            return null;
        }

        // JobEsEntity does not have weight-related attributes yet, need to add weight fields first
        // For example: assessmentWeight, interviewWeight, protoringWeight, skillWeight, softSkillWeight
        CandidateScoreWeights weights = convertToWeights(jobEsEntity);
        
        // Calculate weighted score
        Integer totalScore = ScoreCalculationUtil.calculateWeightedScore(candidateScores, weights);
        
        log.info("Candidate weighted score calculation completed, jobId: {}, totalScore: {}", jobEsEntity.getId(), totalScore);
        return totalScore;
    }
    
    /**
     * 检查候选人分数是否通过阈值
     * 如果有 assessmentScore，则只比较 assessmentScore
     * 如果没有 assessmentScore，则依次比较其他分数，任何一个低于阈值则返回 false
     * 
     * @param jobId 职位ID
     * @param candidateScores 候选人得分
     * @return true表示通过，false表示不通过
     */
    @Override
    public boolean checkThresholdScore(Long jobId, CandidateScores candidateScores) {
        log.info("Start checking candidate score threshold, jobId: {}, candidateScores: {}", jobId, candidateScores);
        
        if (candidateScores == null) {
            log.warn("Candidate scores is null, jobId: {}", jobId);
            return false;
        }
        
        // Get job information
        JobEsEntity jobEsEntity = jobEsService.getJobById(jobId);
        if (jobEsEntity == null) {
            log.warn("Job not found, jobId: {}", jobId);
            return false;
        }
        if (!jobEsEntity.getIntelligenceSwitch()) {
            log.warn("Job intelligence switch is closed, jobId: {}", jobId);
            return true;
        }

        // JobEsEntity does not have threshold-related attributes yet, need to add threshold fields first
        // For example: assessmentThreshold, interviewThreshold, proctoringThreshold, skillThreshold, softSkillThreshold
        CandidateScoreThresholds thresholds = convertToThresholds(jobEsEntity);
        
        // If no assessmentScore, check other scores sequentially
        // Check interviewScore
        if (candidateScores.getInterviewScore() != null) {
            if (!ScoreCalculationUtil.checkThreshold(
                    candidateScores.getInterviewScore(), 
                    thresholds.getInterviewThreshold())) {
                log.info("interviewScore failed threshold check, jobId: {}, interviewScore: {}, threshold: {}", 
                        jobId, candidateScores.getInterviewScore(), thresholds.getInterviewThreshold());
                return false;
            }
        }
        
        // Check proctoringScore
        if (candidateScores.getProctoringScore() != null) {
            if (!ScoreCalculationUtil.checkThreshold(
                    candidateScores.getProctoringScore(),
                    thresholds.getProctoringThreshold())) {
                log.info("proctoringScore failed threshold check, jobId: {}, proctoringScore: {}, threshold: {}",
                        jobId, candidateScores.getProctoringScore(), thresholds.getProctoringThreshold());
                return false;
            }
        }
        
        // Check skillScore
        if (candidateScores.getTechnicalSkillScore() != null) {
            if (!ScoreCalculationUtil.checkThreshold(
                    candidateScores.getTechnicalSkillScore(),
                    thresholds.getTechnicalSkillThreshold())) {
                log.info("skillScore failed threshold check, jobId: {}, skillScore: {}, threshold: {}", 
                        jobId, candidateScores.getTechnicalSkillScore(), thresholds.getTechnicalSkillThreshold());
                return false;
            }
        }
        
        // Check softSkillScore
        if (candidateScores.getSoftSkillScore() != null) {
            if (!ScoreCalculationUtil.checkThreshold(
                    candidateScores.getSoftSkillScore(), 
                    thresholds.getSoftSkillThreshold())) {
                log.info("softSkillScore failed threshold check, jobId: {}, softSkillScore: {}, threshold: {}", 
                        jobId, candidateScores.getSoftSkillScore(), thresholds.getSoftSkillThreshold());
                return false;
            }
        }
        
        log.info("All candidate scores passed threshold check, jobId: {}", jobId);
        return true;
    }
    
    /**
     * Convert JobEsEntity to CandidateScoreWeights
     * 
     * @param jobEsEntity job entity
     * @return weight configuration
     */
    private CandidateScoreWeights convertToWeights(JobEsEntity jobEsEntity) {
        List<IntelligenceScoreRuleDTO> scoreRules = jobEsEntity.getScoreRules();
        if (CollectionUtils.isEmpty(scoreRules)) {
            log.error("Job intelligence judgement score rule is empty, jobId: {}", jobEsEntity.getId());
            return null;
        }
        Integer interviewWeight = 0;
        Integer proctoringWeight = 0;
        Integer technicalSkillWeight = 0;
        Integer softSkillWeight = 0;
        for (IntelligenceScoreRuleDTO scoreRule : scoreRules) {
            if (Objects.equals(JobIntelligenceSubStageEnum.INTERVIEW.getCode(), scoreRule.getSubStageCode())) {
                interviewWeight = scoreRule.getWeight();
                continue;
            }
            if (Objects.equals(JobIntelligenceSubStageEnum.PROCTORING.getCode(), scoreRule.getSubStageCode())) {
                proctoringWeight = scoreRule.getWeight();
                continue;
            }
            if (Objects.equals(JobIntelligenceSubStageEnum.TECHNICAL_SKILLS.getCode(), scoreRule.getSubStageCode())) {
                technicalSkillWeight = scoreRule.getWeight();
                continue;
            }
            if (Objects.equals(JobIntelligenceSubStageEnum.SOFT_SKILLS.getCode(), scoreRule.getSubStageCode())) {
                softSkillWeight = scoreRule.getWeight();
            }
        }
        return CandidateScoreWeights.builder()
                .interviewWeight(interviewWeight)
                .proctoringWeight(proctoringWeight)
                .technicalSkillWeight(technicalSkillWeight)
                .softSkillWeight(softSkillWeight)
                .build();
    }
    
    /**
     * Convert JobEsEntity to threshold configuration
     * 
     * @param jobEsEntity job entity
     * @return threshold configuration
     */
    private CandidateScoreThresholds convertToThresholds(JobEsEntity jobEsEntity) {
        List<IntelligenceScoreRuleDTO> scoreRules = jobEsEntity.getScoreRules();
        if (CollectionUtils.isEmpty(scoreRules)) {
            log.error("Job intelligence judgement score rule is empty, jobId: {}", jobEsEntity.getId());
            return null;
        }
        Integer assessmentThreshold = 0;
        Integer interviewThreshold = 0;
        Integer proctoringThreshold = 0;
        Integer technicalSkillThreshold = 0;
        Integer softSkillThreshold = 0;
        for (IntelligenceScoreRuleDTO scoreRule : scoreRules) {
            if (Objects.equals(JobIntelligenceSubStageEnum.ASSESSMENT_SCORE.getCode(), scoreRule.getSubStageCode())) {
                assessmentThreshold = scoreRule.getThreshold();
            }
            if (Objects.equals(JobIntelligenceSubStageEnum.INTERVIEW.getCode(), scoreRule.getSubStageCode())) {
                interviewThreshold = scoreRule.getThreshold();
            }
            if (Objects.equals(JobIntelligenceSubStageEnum.PROCTORING.getCode(), scoreRule.getSubStageCode())) {
                proctoringThreshold = scoreRule.getThreshold();
            }
            if (Objects.equals(JobIntelligenceSubStageEnum.TECHNICAL_SKILLS.getCode(), scoreRule.getSubStageCode())) {
                technicalSkillThreshold = scoreRule.getThreshold();
            }
            if (Objects.equals(JobIntelligenceSubStageEnum.SOFT_SKILLS.getCode(), scoreRule.getSubStageCode())) {
                softSkillThreshold = scoreRule.getThreshold();
            }
        }

        // Currently return default threshold configuration as placeholder
        return CandidateScoreThresholds.builder()
                .assessmentThreshold(assessmentThreshold)
                .interviewThreshold(interviewThreshold)
                .proctoringThreshold(proctoringThreshold)
                .technicalSkillThreshold(technicalSkillThreshold)
                .softSkillThreshold(softSkillThreshold)
                .build();
    }
    
    /**
     * Candidate score thresholds configuration
     * Inner class to store thresholds for each score
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CandidateScoreThresholds {
        private Integer assessmentThreshold;
        private Integer interviewThreshold;
        private Integer proctoringThreshold;
        private Integer technicalSkillThreshold;
        private Integer softSkillThreshold;
    }
}
