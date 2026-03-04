package com.item.vo;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class IntelligenceScoreRuleVO {

    /**
     * 阶段代码
     * 如: SCREENED, AI_VETTED
     */
    private String stageCode;

    /**
     * 子阶段代码
     * 如: ASSESSMENT_SCORE, INTERVIEW, PROCTORING, TECHNICAL_SKILLS, SOFT_SKILLS, OVERALL
     */
    private String subStageCode;

    /**
     * 权重 (0-100)
     * 各评估维度在整体评分中的占比
     */
    private Integer weight;

    /**
     * 阈值 (0-100)
     * 通过该阶段的达标分数线
     */
    private Integer threshold;
}
