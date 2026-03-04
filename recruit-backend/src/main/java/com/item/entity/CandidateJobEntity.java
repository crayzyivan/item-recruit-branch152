package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 候选人职位关联表
 *
 * @author system
 * @since 2025-07-07
 */
@Data
@TableName("r_candidate_job")
public class CandidateJobEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 候选人ID
     */
    @TableField("candidate_id")
    private Long candidateId;

    /**
     * 职位ID
     */
    @TableField("job_id")
    private Long jobId;

    /**
     * 公司ID
     */
    @TableField("customer_id")
    private Long customerId;

    /**
     * 求职信
     */
    @TableField("cover_letter")
    private String coverLetter;

    /**
     * 是否送达(0:未送达;1:已送达)
     */
    @TableField("posted")
    private Integer posted;

    /**
     * 简历申请状态(0:未被查看;1:已送达;2:录用;-1:拒绝)
     */
    @TableField("apply_status")
    private Integer applyStatus;

    @TableField("company_code")
    private String companyCode;

    /**
     * 原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 是否被邀请重新申请(0:未邀请,1:已邀请)
     */
    @TableField("reapply_invited")
    private Integer reapplyInvited;

    /**
     * 逻辑删除标识(0:未删除;1:已删除)
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 筛选建议Deny Outright、Proceed with Application
     */
    private Long recommendation;

    /**
     * 候选人期望AI电话面试开始时间
     */
    private String preferredInterviewStartTime;

    /**
     * 候选人期望AI电话面试结束时间
     */
    private String preferredInterviewEndTime;
    /**
     * 候选人面试用电话
     */
    private String interviewPhone;
    /**
     * 候选人电话面试语言
     */
    private String interviewLanguage;
    /**
     * ai评分
     */
    private Integer assessmentScore;
    /**
     * 是否已发送ai面试邮件(0:未发送，1：已发送)
     */
    private Integer interviewMailStatus;
    /**
     * 是否已预定 ai 电话（0:未预定，1：已预定）
     */
    private Integer interviewPhoneStatus;
    /**
     * ai面试开始时间
     */
    private LocalDateTime interviewStartTime;
    /**
     * ai面试结束时间
     */
    private LocalDateTime interviewEndTime;
    /**
     * 应聘者参加面试时间
     */
    private LocalDateTime interviewTime;
    /**
     * ai面试评分
     */
    private Integer overallScore;
    /**
     * ai面试作弊分析得分
     */
    private Integer cheatingAnalysis;
    /**
     * ai面试视频地址
     */
    private String cameraRecordingUrl;

    /**
     * AI电话面试地址
     */
    private String phoneRecordingUrl;
    /**
     * 候选人所在位置
     */
    private String location;

    private Long jobCountryId;
    private Long jobStateId;
    private Long jobCityId;
    private String jobCountryName;
    private String jobStateName;
    private String jobCityName;

    /**
     * Google Map Place Id
     */
    private String placeId;
    /**
     * Naukri application id (nullable)
     */
    @TableField("naukri_application_id")
    private String naukriApplicationId;

    private String interviewUrl;

    /**
     * 申请方式(0:平台申请;1:邀请面试自动投递)
     */
    @TableField("apply_method")
    private Integer applyMethod;

    private String questionInfo;

}