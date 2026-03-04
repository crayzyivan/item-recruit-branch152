package com.item.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CandidateEducationDTO {
    private Long institutionTypeId;
    private String institutionName;
    private Integer graduated;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long degreeId;
    private String major;
    private String minor;
} 