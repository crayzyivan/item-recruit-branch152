package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.item.dto.job.IntelligenceScoreRuleDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 简历岗位匹配度
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobMatchResultVO implements Serializable {
    /**
     * 岗位申请id
     */
    private Long id;
    /**
     * 候选人邮箱
     */
    private String candidateEmail;
    /**
     * 简历URL
     */
    private String resumeUrl;

    /**
     * 电话号码
     */
    private String phoneNumber;

    /**
     * 候选人ID
     */
    private Long candidateId;

    private String candidateName;



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
    private Integer jobTitleScore;
    private String jobTitleResult;
    private Integer skillsScore;
    private String skillsResult;
    private Integer requirementScore;
    private String requirementResult;
    private Integer responsibilityScore;
    private String responsibilityResult;
    private Integer experienceScore;
    private String experienceResult;
    private Integer locationScore;
    private String locationResult;
    private Integer minimumSalaryScore;
    private String minimumSalaryResult;
    private Integer maximumSalaryScore;
    private String maximumSalaryResult;
    private String summary;
    private String strengths;
    private String weaknesses;
    private String analysis;
    private String comments;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
    private LocalDateTime candidateCreateTime;
    private LocalDateTime candidateUpdateTime;
    private String middleName;
    private String gender;
    private LocalDate dateOfBirth;
    private String streetAddress;
    private String apartmentOrSuite;
    private Long candidateCountryId;
    private Long candidateStateId;
    private Long candidateCityId;
    private String candidateCountryName;
    private String candidateStateName;
    private String candidateCityName;
    private String postalCode;
    private Long currencyTypeId;
    private Integer expectedSalary;
    private Long salaryTypeId;
    private LocalDate availableFrom;
    private Integer uploadStatus;
    private String title;
    private Long masterAccountId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer locationId;
    private String placeId;
    private Integer jobStatus;
    private Integer needListed;
    private String urlCode;
    private Integer typeId;
    private Integer categoryId;
    private Integer modeId;
    private Integer salaryType;
    private Integer currency;
    private Integer numberOpenings;
    private LocalDateTime jobCreateTime;
    private LocalDateTime jobUpdateTime;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer hotList;
    private Boolean jobDeleted;
    private String locationName;
    private String companyTitleHash;
    private String interviewUrlId;
    private String companyCode;
    private Integer applyStatus;
    private String salaryTypeName;
    private String currencyName;
    private String applyStatusName;
    private String jobCurrencyName;
    /**
     * 智能评分规则列表
     * 存储各阶段评估维度的权重和阈值配置
     */
    private List<IntelligenceScoreRuleDTO> scoreRules;

    public void convertJobSalaryTypeNameByLanguage(Map<Long, String> nameMap){
        this.setSalaryTypeName(nameMap.getOrDefault(this.getSalaryType() != null ? this.getSalaryType().longValue() : 0L, this.getSalaryTypeName()));
    }
}
