package com.item.pgentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("candidates.candidate_data")
public class CandidateDataEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的申请ID
     * 外键关联到candidates.applications表
     */
    @TableField("application_id")
    private String applicationId;

    /**
     * 报告唯一标识
     */
    @TableField("report_id")
    private String reportId;

    /**
     * 报告访问URL
     */
    @TableField("report_url")
    private String reportUrl;

    /**
     * 面试录音URL
     */
    @TableField("interview_recording_url")
    private String interviewRecordingUrl;

    /**
     * 面试总分
     */
    @TableField("interview_score")
    private Integer interviewScore;

    /**
     * 监考分数
     */
    @TableField("proctoring_score")
    private Integer proctoringScore;

    /**
     * 报告生成日期
     */
    @TableField("report_date")
    private OffsetDateTime reportDate;

    /**
     * 面试转录文本（JSON格式）
     * PostgreSQL JSONB类型
     */
    @TableField("interview_transcript")
    private String interviewTranscript;

    /**
     * 技术技能评估（JSON格式）
     * PostgreSQL JSONB类型
     */
    @TableField("technical_skills_evaluation")
    private String technicalSkillsEvaluation;

    /**
     * 软技能评估（JSON格式）
     * PostgreSQL JSONB类型
     */
    @TableField("soft_skills_evaluation")
    private String softSkillsEvaluation;

    /**
     * 编程技能评估（JSON格式）
     * PostgreSQL JSONB类型
     */
    @TableField("coding_skills_evaluation")
    private String codingSkillsEvaluation;

    /**
     * 自定义问题评估（JSON格式）
     * PostgreSQL JSONB类型
     */
    @TableField("custom_question_evaluation")
    private String customQuestionEvaluation;

    /**
     * 监考违规记录（JSON格式）
     * PostgreSQL JSONB类型
     */
    @TableField("proctoring_violations")
    private String proctoringViolations;

    /**
     * 创建时间
     * 默认值为now()
     */
    @TableField("created_on")
    private OffsetDateTime createdOn;
}
