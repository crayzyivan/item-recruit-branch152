package com.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.item.framework.annotation.Xss;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CandidateRequestVO {
    @NotBlank(message = "The email cannot be empty")
    @Email(message = "The email format is incorrect")
    private String email;
    @Size(max = 50, message = "The Middle Name must not exceed 50 characters")
    @Xss(message = "Middle name special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String middleName;
    @NotBlank(message = "The First Name cannot be empty")
    @Size(max = 50, message = "The First Name must not exceed 50 characters")
    @Xss(message = "First name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String firstName;
    @NotBlank(message = "The Last Name cannot be empty")
    @Size(max = 50, message = "The Last Name must not exceed 50 characters")
    @Xss(message = "Last name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String lastName;
    @NotBlank(message = "The gender cannot be empty")
    @Xss(message = "Gender some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String gender;
    @Size(max = 20, message = "The phone must not exceed 20 characters")
    @NotBlank(message = "The phone cannot be empty")
    @JsonProperty("phone")
    @Xss(message = "Phone number some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String phoneNumber;
    @NotNull(message = "The Date of Birth cannot be empty")
    private LocalDate dateOfBirth;
    @Size(max = 255, message = "The Street Address must not exceed 255 characters")
    @NotBlank(message = "The Street Address cannot be empty")
    @Xss(message = "Street address some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String streetAddress;
    @Size(max = 255, message = "The Apartment/Suite must not exceed 255 characters")
    @Xss(message = "Apartment or suite some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String apartmentOrSuite;
    @NotNull(message = "The Country ID cannot be empty")
    private Long countryId;
    @NotNull(message = "The State ID cannot be empty")
    private Long stateId;
    @NotNull(message = "The City ID cannot be empty")
    private Long cityId;
    @Size(max = 200, message = "countryName length must be less than or equal to {max}")
    @Xss(message = "Country name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String countryName;
    @Size(max = 200, message = "stateName length must be less than or equal to {max}")
    @Xss(message = "State name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String stateName;
    @Size(max = 200, message = "cityName length must be less than or equal to {max}")
    @Xss(message = "City name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String cityName;
    @Size(max = 200, message = "postalCode length must be less than or equal to {max}")
    @Xss(message = "Postal code some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String postalCode;
    @NotNull(message = "The Currency Type ID cannot be empty")
    private Long currencyTypeId;
    @Max(value = 1000000000, message = "The expected salary must not exceed 1,000,000,000.")
    private Integer expectedSalary;
    @NotNull(message = "The Salary Type ID cannot be empty")
    private Long salaryTypeId;
    @NotNull(message = "The Available From cannot be empty")
    private LocalDate availableFrom;
    @Size(max = 20,message = "educationList length must be less than or equal to {max}")
    @Valid
    private List<CandidateEducationVO> educationList;
    @Size(max = 20,message = "employmentList length must be less than or equal to {max}")
    @Valid
    private List<EmploymentHistoryVO> employmentList;
    //@NotNull(message = "The resumeUrl From cannot be empty")
    @Xss(message = "resumeUrl name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String resumeUrl;


}