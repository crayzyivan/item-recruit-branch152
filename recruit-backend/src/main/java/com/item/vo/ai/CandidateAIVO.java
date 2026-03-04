package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAIVO {
    private String candidateName;
    private String candidateEmail;
    private String phoneNumber;
    private String middleName;
    private String gender;
    private LocalDate dateOfBirth;
    private String streetAddress;
    private String apartmentOrSuite;
    private String countryName;
    private String stateName;
    private String cityName;
    private String postalCode;
    @JsonIgnore
    private Long currencyTypeId;
    private String currencyType;
    @JsonIgnore
    private Long expectedSalaryId;
    private String expectedSalary;
    @JsonIgnore
    private Long salaryTypeId;
    private String salaryType;
    private LocalDate availableFrom;
    private List<CandidateEducationAIVO> candidateEducations;
    private List<EmploymentHistoryAIVO> employmentHistories;
}