package com.item.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CandidateProfileEmploymentVO {
    private String companyName;
    private Boolean currentEmployer;
    private String jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String responsibilities;
} 