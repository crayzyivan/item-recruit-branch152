package com.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.item.framework.annotation.Xss;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmploymentHistoryVO {
    @Size(max = 200, message = "companyName length must be less than or equal to {max}")
    @Xss(message = "Company name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String companyName;
    @Size(max = 200, message = "currentEmployer length must be less than or equal to {max}")
    @Xss(message = "Current employer some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String currentEmployer;
    @Size(max = 200, message = "jobTitle length must be less than or equal to {max}")
    @Xss(message = "Job title some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    @JsonProperty("responsibilities")
    @Size(max = 2000, message = "keyResponsibilities length must be less than or equal to {max}")
    @Xss(message = "Responsibilities some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String keyResponsibilities;
} 