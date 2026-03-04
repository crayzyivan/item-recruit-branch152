package com.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.item.framework.annotation.Xss;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 邀请面试工作经历VO
 * 用于接收前端调用"/resume-parsing"接口后的工作经历解析结果
 *
 * @since 2025-11-13
 */
@Data
public class InviteInterviewEmploymentHistoryVO {
    
    /**
     * 公司名称（可选）
     */
    @Size(max = 200, message = "Company name length must be less than or equal to 200")
    @Xss(message = "Company name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String companyName;
    
    /**
     * 当前雇主（可选）
     */
    @Size(max = 200, message = "Current employer length must be less than or equal to 200")
    @Xss(message = "Current employer some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String currentEmployer;
    
    /**
     * 职位名称（可选）
     */
    @Size(max = 200, message = "Job title length must be less than or equal to 200")
    @Xss(message = "Job title some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String jobTitle;
    
    /**
     * 开始日期（可选）
     */
    private LocalDate startDate;
    
    /**
     * 结束日期（可选）
     */
    private LocalDate endDate;
    
    /**
     * 主要职责（可选）
     */
    @JsonProperty("responsibilities")
    @Size(max = 2000, message = "Key responsibilities length must be less than or equal to 2000")
    @Xss(message = "Responsibilities some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String keyResponsibilities;
}

