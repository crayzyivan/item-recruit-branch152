package com.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.item.framework.annotation.Xss;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 邀请面试简历解析结果VO
 * 用于接收前端调用"/resume-parsing"接口后的解析结果
 * 
 * 必传字段说明（基于功能需求）：
 * - email: 背景调查、AI简历匹配、获取面试链接功能必需
 * - firstName, lastName: 背景调查功能必需
 * - gender: 候选人信息完整性必需
 * - phoneNumber: 背景调查、AI简历匹配、获取面试链接功能必需
 * - dateOfBirth: 背景调查功能必需
 * 
 * 其他字段为可选，但建议提供以提高AI匹配准确度和后续流程效率
 *
 * @since 2025-11-13
 */
@Data
public class InviteInterviewParsedResumeVO {
    
    /**
     * 邮箱（必传）
     * 用于背景调查、AI简历匹配、获取面试链接功能
     * 应该与请求中的candidateEmail一致
     */
    @NotBlank(message = "Email is required for background check, AI matching and interview link generation")
    @Email(message = "Invalid email format")
    private String email;
    
    /**
     * 中间名（可选）
     */
    @Size(max = 50, message = "The Middle Name must not exceed 50 characters")
    @Xss(message = "Middle name special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String middleName;
    
    /**
     * 名（必传）
     * 用于背景调查功能
     */
    @NotBlank(message = "First Name is required for background check")
    @Size(max = 50, message = "The First Name must not exceed 50 characters")
    @Xss(message = "First name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String firstName;
    
    /**
     * 姓（必传）
     * 用于背景调查功能
     */
    @NotBlank(message = "Last Name is required for background check")
    @Size(max = 50, message = "The Last Name must not exceed 50 characters")
    @Xss(message = "Last name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String lastName;
    
    /**
     * 性别（必传）
     * 用于候选人信息完整性
     */
    @NotBlank(message = "Gender is required")
    @Xss(message = "Gender some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String gender;
    
    /**
     * 电话号码（必传）
     * 用于背景调查、AI简历匹配、获取面试链接功能
     */
    @NotBlank(message = "Phone Number is required for background check, AI matching and interview link generation")
    @Size(max = 20, message = "The phone must not exceed 20 characters")
    @JsonProperty("phone")
    @Xss(message = "Phone number some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String phoneNumber;
    
    /**
     * 出生日期（必传）
     * 用于背景调查功能
     */
    @NotNull(message = "Date of Birth is required for background check")
    private LocalDate dateOfBirth;
    
    /**
     * 街道地址（可选）
     */
    @Size(max = 255, message = "The Street Address must not exceed 255 characters")
    @Xss(message = "Street address some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String streetAddress;
    
    /**
     * 公寓/套房（可选）
     */
    @Size(max = 255, message = "The Apartment/Suite must not exceed 255 characters")
    @Xss(message = "Apartment or suite some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String apartmentOrSuite;
    
    /**
     * 国家ID（可选）
     * 如果提供，必须大于0
     */
    @Min(value = 1, message = "Country ID must be greater than or equal to 1")
    private Long countryId;
    
    /**
     * 州/省ID（可选）
     * 如果提供，必须大于0
     */
    @Min(value = 1, message = "State ID must be greater than or equal to 1")
    private Long stateId;
    
    /**
     * 城市ID（可选）
     * 如果提供，必须大于0
     */
    @Min(value = 1, message = "City ID must be greater than or equal to 1")
    private Long cityId;
    
    /**
     * 国家名称（可选）
     */
    @Size(max = 200, message = "Country name length must be less than or equal to 200")
    @Xss(message = "Country name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String countryName;
    
    /**
     * 州/省名称（可选）
     */
    @Size(max = 200, message = "State name length must be less than or equal to 200")
    @Xss(message = "State name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String stateName;
    
    /**
     * 城市名称（可选）
     */
    @Size(max = 200, message = "City name length must be less than or equal to 200")
    @Xss(message = "City name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String cityName;
    
    /**
     * 邮政编码（可选）
     */
    @Size(max = 200, message = "Postal code length must be less than or equal to 200")
    @Xss(message = "Postal code some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String postalCode;
    
    /**
     * 货币类型ID（可选）
     */
    private Long currencyTypeId;
    
    /**
     * 期望薪资（可选）
     * 如果提供，不能超过1,000,000,000
     */
    @Max(value = 1000000000, message = "The expected salary must not exceed 1,000,000,000")
    private Integer expectedSalary;
    
    /**
     * 薪资类型ID（可选）
     */
    private Long salaryTypeId;

    /**
     * 可入职日期（可选）
     */
    private LocalDate availableFrom;
    
    /**
     * 教育经历列表（可选）
     * 如果提供，最多20条
     */
    @Size(max = 20, message = "Education list must not exceed 20 items")
    @Valid
    private List<InviteInterviewEducationVO> educationList;
    
    /**
     * 工作经历列表（可选）
     * 如果提供，最多20条
     */
    @Size(max = 20, message = "Employment list must not exceed 20 items")
    @Valid
    private List<InviteInterviewEmploymentHistoryVO> employmentList;
    
    /**
     * 简历URL（可选）
     */
    @Xss(message = "Resume URL some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String resumeUrl;
}

