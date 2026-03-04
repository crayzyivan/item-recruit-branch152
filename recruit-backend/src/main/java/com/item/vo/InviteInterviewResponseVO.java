package com.item.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邀请面试响应VO
 * 返回申请ID和候选人ID等信息
 *
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteInterviewResponseVO {
    
    /**
     * 申请ID（r_candidate_job表的主键id）
     */
    private Long applicationId;
    
    /**
     * 候选人ID（r_candidate表的主键id）
     */
    private Long candidateId;
    
    /**
     * 响应消息
     */
    private String message;
}

