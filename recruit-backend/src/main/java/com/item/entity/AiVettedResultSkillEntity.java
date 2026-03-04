package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI审核技能明细表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:03
 */
@Data
@TableName("r_ai_vetted_result_skill")
public class AiVettedResultSkillEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(value = "vetted_result_id")
    private Long vettedResultId;
    @TableField(value = "skill_name")
    private String skillName;
    @TableField(value = "skill_score")
    private Integer skillScore;
    /**
     * 技能评级(0:没有经历;10:初级;20:中级;30:高级;)
     */
    @TableField(value = "skill_level")
    private Integer skillLevel;
    @TableField(value = "skill_assessment")
    private String skillAssessment;

    @TableField(value = "create_time")
    private LocalDateTime createTime;
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}