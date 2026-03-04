package com.item.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 简历筛选和面试结果筛选计算工具类
 * 提供阈值判断和权重计算功能
 */
public class ScoreCalculationUtil {

    /**
     * 阈值判断方法
     * 比较得分是否大于等于阈值
     *
     * @param score 得分
     * @param threshold 阈值
     * @return true表示通过（得分>=阈值），false表示不通过（得分<阈值）
     */
    public static boolean checkThreshold(Integer score, Integer threshold) {
        if (score == null || threshold == null) {
            return false;
        }
        return score >= threshold;
    }

    /**
     * 权重计算方法
     * 根据候选人的5个分数和对应权重，计算加权总分
     * 权重采用百分制（50表示50%），计算结果四舍五入取整
     *
     * @param scores 候选人得分情况
     * @param weights 权重配置（百分制）
     * @return 加权总分（四舍五入后的整数）
     */
    public static Integer calculateWeightedScore(CandidateScores scores, CandidateScoreWeights weights) {
        if (scores == null || weights == null) {
            return null;
        }

        BigDecimal totalScore = null;

        // 面试分数加权
        if (scores.getInterviewScore() != null && weights.getInterviewWeight() != null) {
            totalScore = BigDecimal.valueOf(scores.getInterviewScore())
                    .multiply(BigDecimal.valueOf(weights.getInterviewWeight()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // 监考分数加权
        if (scores.getProctoringScore() != null && weights.getProctoringWeight() != null) {
            BigDecimal weightedScore = BigDecimal.valueOf(scores.getProctoringScore())
                    .multiply(BigDecimal.valueOf(weights.getProctoringWeight()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalScore = totalScore == null ? weightedScore : totalScore.add(weightedScore);
        }

        // 技能分数加权
        if (scores.getTechnicalSkillScore() != null && weights.getTechnicalSkillWeight() != null) {
            BigDecimal weightedScore = BigDecimal.valueOf(scores.getTechnicalSkillScore())
                    .multiply(BigDecimal.valueOf(weights.getTechnicalSkillWeight()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalScore = totalScore == null ? weightedScore : totalScore.add(weightedScore);
        }

        // 软技能分数加权
        if (scores.getSoftSkillScore() != null && weights.getSoftSkillWeight() != null) {
            BigDecimal weightedScore = BigDecimal.valueOf(scores.getSoftSkillScore())
                    .multiply(BigDecimal.valueOf(weights.getSoftSkillWeight()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalScore = totalScore == null ? weightedScore : totalScore.add(weightedScore);
        }

        if (totalScore == null) {
            return null;
        }
        // 四舍五入取整
        return totalScore.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
