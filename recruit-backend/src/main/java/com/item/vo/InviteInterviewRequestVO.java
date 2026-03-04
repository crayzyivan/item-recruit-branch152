package com.item.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 邀请面试请求VO
 * 用于HR邀请外部候选人面试
 * 注意：简历解析由前端调用"/resume-parsing"接口完成，解析结果通过parsedResume字段回传
 *
 * @since 2025-11-13
 */
@Data
public class InviteInterviewRequestVO {
    
    /**
     * 职位ID（必填）
     */
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    /**
     * 候选人邮箱（必填）
     */
    @Email(message = "Invalid email format")
    private String candidateEmail;
    
    /**
     * 候选人姓名（必填）
     */
    private String candidateName;
    
    /**
     * 简历解析结果（可选）
     * 前端调用"/resume-parsing"接口后，将解析结果回传
     * 如果提供了此字段，将使用解析结果中的信息（如firstName、lastName、phoneNumber等）
     */
    @NotNull(message = "parsedResume is required")
    private InviteInterviewParsedResumeVO parsedResume;
    
    /**
     * 职位国家ID（必填）
     */
    @NotNull(message = "Job country ID is required")
    @Min(value = 1, message = "Job country ID must be greater than or equal to 1")
    private Long jobCountryId;
    
    /**
     * 职位州/省ID（必填）
     */
//    @NotNull(message = "Job state ID is required")
//    @Min(value = 1, message = "Job state ID must be greater than or equal to 1")
    private Long jobStateId;
    
    /**
     * 职位城市ID（必填）
     */
//    @NotNull(message = "Job city ID is required")
//    @Min(value = 1, message = "Job city ID must be greater than or equal to 1")
    private Long jobCityId;

    /**
     * Google Place Id
     */
    private String placeId;

    /**
     * 职位国家名称（可选）
     * 如果提供，将直接使用；如果未提供，将根据jobCountryId查询
     */
    private String jobCountryName;
    
    /**
     * 职位州/省名称（可选）
     * 如果提供，将直接使用；如果未提供，将根据jobStateId查询
     */
    private String jobStateName;
    
    /**
     * 职位城市名称（可选）
     * 如果提供，将直接使用；如果未提供，将根据jobCityId查询
     */
    private String jobCityName;
}

