package com.item.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 候选人个人资料视图对象
 * 用于候选人查看自己的完整个人信息
 * </p>
 *
 * @author yunlong.li on 2025/09/09
 * @since 1.0.0
 */
@Data
public class CandidateProfileVO {
    private Long id;
    private String email;
    private String middleName;
    private String firstName;
    private String lastName;
    private String gender;
    private String genderName;
    private String phoneNumber;
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
    private String currencyTypeName;
    private Integer expectedSalary;
    private Long salaryTypeId;
    private String salaryTypeName;
    private LocalDate availableFrom;
    private String resumeName;
    private String resumeUrl;
    private List<CandidateProfileEducationVO> educationList;
    private List<CandidateProfileEmploymentVO> employmentList;


}
