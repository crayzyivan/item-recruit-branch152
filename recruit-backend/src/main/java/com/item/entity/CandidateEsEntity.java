package com.item.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 候选人实体类
 * 对应数据库表：r_candidate
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateEsEntity {
    /**
     * 主键ID
     */
    private Long id;
    private String candidateName;
    private String candidateEmail;
    private String resumeUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String phoneNumber;
    private Integer deleted;
    private String middleName;
    private String gender;
    private LocalDate dateOfBirth;
    private String streetAddress;
    private String apartmentOrSuite;
    private Long countryId;
    private Long stateId;
    private Long cityId;
    private String countryName;
    private String stateName;
    private String cityName;
    private String postalCode;
    private Long educationId;
    private Long employmentId;
    private Long currencyTypeId;
    private Integer expectedSalary;
    private Long salaryTypeId;
    private LocalDate availableFrom;
    private List<CandidateEducationEntity> candidateEducations;
    private List<EmploymentHistoryEntity> employmentHistories;
    private String resumeContent;
    private Integer uploadStatus;
}