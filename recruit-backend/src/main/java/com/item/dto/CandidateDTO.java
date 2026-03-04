package com.item.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CandidateDTO {
    private Long id;
    private String candidateName;
    private String firstName;
    private String lastName;
    private String candidateEmail;
    private String password;
    private String resumeUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String phoneNumber;
    private String ext1;
    private String ext2;
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
    private Long currencyTypeId;
    private Integer expectedSalary;
    private Long salaryTypeId;
    private LocalDate availableFrom;
    private List<CandidateEducationDTO> educationList;
    private List<EmploymentHistoryDTO> employmentList;
    private String candidatePermanentEmail;
} 