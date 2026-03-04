package com.item.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量邀请面试单项结果VO
 * 表示单个候选人的邀请处理结果
 *
 * @since 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchInviteInterviewItemVO {

    /**
     * 对应数据行索引
     */
    private Integer index;
    /**
     * 职位ID
     */
    private Long jobId;
    
    /**
     * 候选人邮箱
     */
    private String candidateEmail;
    
    /**
     * 候选人姓名
     */
    private String candidateName;
    
    /**
     * 申请ID（成功时返回）
     */
    private Long applicationId;
    
    /**
     * 候选人ID（成功时返回）
     */
    private Long candidateId;
    
    /**
     * 处理状态（SUCCESS/FAILURE）
     */
    private String status;
    
    /**
     * 消息（成功或失败原因）
     */
    private String message;
    
    /**
     * 错误码（失败时返回）
     */
    private Integer errorCode;
}
