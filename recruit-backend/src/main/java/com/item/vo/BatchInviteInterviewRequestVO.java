package com.item.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量邀请面试请求VO
 * 用于HR批量邀请多个外部候选人面试
 *
 * @since 2025-01-15
 */
@Data
public class BatchInviteInterviewRequestVO {
    
    /**
     * 邀请面试请求列表（必填）
     * 至少包含1个，最多100个
     */
    @NotEmpty(message = "Invite list cannot be empty")
    @Size(min = 1, max = 100, message = "Invite list size must be between 1 and 100")
    @Valid
    private List<InviteInterviewRequestVO> inviteList;
}
