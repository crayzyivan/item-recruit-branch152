package com.item.dto.job;

import lombok.Data;

/**
 * 智能评分规则 DTO
 * Service/Business/ES 层使用，用于在各层之间传递智能评估配置信息
 *
 * @author system
 */
@Data
public class IntelligenceScoreRuleDTO {

    /**
     * 阶段代码
     * 如: SCREENED, AI_VETTED
     */
    private String stageCode;

    /**
     * 阶段顺序
     */
    private Integer stageOrder;

    /**
     * 子阶段代码
     * 如: ASSESSMENT_SCORE, INTERVIEW, PROCTORING, TECHNICAL_SKILLS, SOFT_SKILLS
     */
    private String subStageCode;

    /**
     * 子阶段顺序
     */
    private Integer subStageOrder;

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
