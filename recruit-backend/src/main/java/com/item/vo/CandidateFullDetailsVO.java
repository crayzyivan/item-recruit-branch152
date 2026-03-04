package com.item.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.item.entity.CandidateEducationEntity;
import com.item.entity.EmploymentHistoryEntity;
import lombok.Data;

/**
 * <p>
 * 全量候选人信息
 * </p>
 *
 * @author liuyabin on 2025/8/26
 * @since 1.0.0
 */
@Data
public class CandidateFullDetailsVO {
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
