package com.item.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 候选人职位关联表
 *
 * @author system
 * @since 2025-07-07
 */
@Data
@Builder
public class ResumeAIMatchDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 候选人ID
     */
    private Long candidateId;

    /**
     * 候选人名称
     */
    private String candidateName;

    /**
     * 候选人邮箱
     */
    private String candidateEmail;

    /**
     * 职位ID
     */
    private Long jobId;

    /**
     * 公司ID
     */
    private Long customerId;


    /**
     * 筛选建议Deny Outright、Proceed with Application
     */
    private Long recommendation;

    /**
     * ai评分
     */
    private Integer assessmentScore;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否已发送ai面试邮件(0:未发送，1：已发送)
     */
    private Integer interviewMailStatus;

    private LocalDateTime lastSendEmailTime;

    private Integer interviewPhoneStatus;

    private LocalDateTime lastAppointmentCallTime;

    private String preferredInterviewStartTime;

    private String preferredInterviewEndTime;

    private Integer score;
}