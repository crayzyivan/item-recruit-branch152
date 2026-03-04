package com.item.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CandidateProfileEducationVO {
    private Long institutionType;
    private String institutionName;
    private Integer graduated;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long degree;
    private String degreeName;
    private String major;
    private String minor;
} 