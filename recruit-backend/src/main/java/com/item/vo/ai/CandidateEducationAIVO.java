package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CandidateEducationAIVO {
    private String institutionName;
    @JsonIgnore
    private Long graduatedId;
    private String graduated;
    private LocalDate startDate;
    private LocalDate endDate;
    @JsonIgnore
    private Long degreeId;
    private String degree;
    private String major;
    private String minor;
} 