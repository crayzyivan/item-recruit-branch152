package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI审核结果表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:03
 */
@Data
@TableName("r_ai_vetted_result")
public class AiVettedResultEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(value = "candidate_job_id")
    private Long candidateJobId;
    @TableField(value = "interview_time")
    private LocalDateTime interviewTime;
    @TableField(value = "interview_score")
    private Integer interviewScore;
    @TableField(value = "proctoring_score")
    private Integer proctoringScore;
    @TableField(value = "overall_skill_assessment")
    private String overallSkillAssessment;
    @TableField(value = "overall_skill_score")
    private Integer overallSkillScore;
    @TableField(value = "personality_score")
    private Integer personalityScore;
    /**
     * 技能总体评级(0:没有经历;10:初级;20:中级;30:高级;)
     */
    @TableField(value = "overall_skill_level")
    private Integer overallSkillLevel;

    /**
     * 根据 r_ai_vetted_result_skill 排除 communication 其余项计算得出
     */
    @TableField(value = "technical_skill_score")
    private Integer technicalSkillScore;

    /**
     * 软技能得分从 r_ai_vetted_result_skill 表中 communication 计算得出
     */
    @TableField(value = "soft_skill_score")
    private Integer softSkillScore;
    /**
     * 根据权重计算得出
     */
    @TableField(value = "overall_score")
    private Integer overallScore;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "transcript")
    private String transcript;
    @TableField(value = "camera_recording_url")
    private String cameraRecordingUrl;
    @TableField(value = "summarized_video_url")
    private String summarizedVideoUrl;
    @TableField(value = "phone_recording_url")
    private String phoneRecordingUrl;

    private String interviewReportUrl;

    private Integer interviewType;

    /**
     * 数据来源 0:Recruit 2:菲律宾
     */
    private Integer dataSource;

}