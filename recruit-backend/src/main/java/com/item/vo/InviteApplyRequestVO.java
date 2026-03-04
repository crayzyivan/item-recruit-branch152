package com.item.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 邀请投递请求VO
 * 用于HR邀请外部候选人投递职位
 *
 * @since 2025-11-13
 */
@Data
public class InviteApplyRequestVO {

    /**
     * 职位ID（必填）
     */
    @NotNull(message = "Job ID is required")
    @Min(value = 1, message = "Job ID must be greater than or equal to 1")
    private Long jobId;

    /**
     * 候选人邮箱（必填）
     */
    @NotBlank(message = "Candidate email is required")
    @Email(message = "Invalid email format")
    private String candidateEmail;

    /**
     * 候选人姓名（必填）
     */
    @NotBlank(message = "Candidate name is required")
    private String candidateName;
}

