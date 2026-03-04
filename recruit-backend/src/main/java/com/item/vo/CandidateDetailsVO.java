package com.item.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 候选人详情
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-25  14:41
 */
@Data
public class CandidateDetailsVO {

    private String candidateName;
    private String candidateEmail;
    private String countryName;
    private String stateName;
    private String cityName;
    private String phoneNumber;
    //出生日期
    private LocalDate dateOfBirth;
    private String gender;
    //简历url
    private String resumeUrl;
    private String createTime;
    //教育经历
    List<EducationDetailsVO> educationList;
    //工作经历
    List<EmploymentDetailsVO> employmentList;

    //candidateId r_candidate.id
    private Long candidateId;

    private Integer expectedSalary;
    private String currencyTypeName;
    private String salaryTypeName;
}