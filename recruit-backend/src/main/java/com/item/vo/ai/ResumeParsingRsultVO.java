package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeParsingRsultVO {
    private String email;
    private String middleName;
    private String firstName;
    private String lastName;
    private String gender;
    @JsonProperty("phone")
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String streetAddress;
    private String apartmentOrSuite;
    private String countryName;
    private String stateName;
    private String cityName;
    private String postalCode;
    private String currencyType; // 货币类型，需要根据字典表转换成id返回给前端
    private Integer expectedSalary;
    private String salaryType;  //薪资类型，需要根据字典表转换成id返回给前端
    private LocalDate availableFrom;
    private List<ResumeCandidateEducationVO> educationList;
    private List<ResumeEmploymentHistoryVO> employmentList;


}