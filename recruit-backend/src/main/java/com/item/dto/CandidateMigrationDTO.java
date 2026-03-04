package com.item.dto;

import com.item.entity.CandidateEducationEntity;
import com.item.entity.EmploymentHistoryEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 应聘者数据迁移DTO
 * 用于在数据迁移过程中传递应聘者信息
 * 
 * @author system
 */
@Data
public class CandidateMigrationDTO {
    
    // PostgreSQL候选人ID
    private String pgCandidateId;
    
    // 用户ID
    private String userId;
    
    // 候选人标识
    private String slug;
    
    // 用户基本信息
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    
    // 应聘者信息
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    
    // 社交媒体信息
    private String facebook;
    private String linkedin;
    private String github;
    
    // 教育信息
    private String institutionType;
    private String institutionName;
    private String degreeEarned;
    private String major;
    private String minor;
    private LocalDate educationStartDate;
    private LocalDate educationEndDate;
    private Boolean graduated;
    
    // 工作信息
    private String companyName;
    private String jobTitle;
    private LocalDate employmentStartDate;
    private LocalDate employmentEndDate;
    private Boolean currentEmployer;
    private String responsibilities;
    
    // 简历信息
    private String resumeFileName;
    private OffsetDateTime resumeCreatedOn;
    
    // 申请信息（最新一条）
    private BigDecimal expectedSalary;
    private String salaryType;
    private OffsetDateTime availableFrom;
    private Integer currencyId;
    
    // 时间信息
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
    private String createdBy;
    private String updatedBy;
    
    // 转换后的实体列表
    private List<CandidateEducationEntity> candidateEducations;
    private List<EmploymentHistoryEntity> employmentHistories;
}
