package com.item.dto;

import lombok.Data;

/**
 * ai面试结果技能明细
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:41
 */
@Data
public class AiVettedResultSkillDTO {
    private String skillName;
    private Integer skillScore;
    private Integer skillLevel;
}