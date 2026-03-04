package com.item.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmploymentHistoryDTO {
    private String companyName;
    private String currentEmployer;
    private String jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyResponsibilities;
} 