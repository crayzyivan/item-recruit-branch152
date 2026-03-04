package com.item.vo;

import lombok.Data;

/**
 * ai面试结果技能明细
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  19:09
 */
@Data
public class AiVettedResultSkillVO {
    private String skillName;
    private Integer skillScore;
    private Integer skillLevel;
    private String skillAssessment;
    private String levelName;
}