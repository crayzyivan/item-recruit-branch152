package com.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.item.framework.annotation.Xss;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CandidateEducationVO {
    @JsonProperty("institutionType")
    private Long institutionTypeId;
    @Size(max = 200, message = "institutionName length must be less than or equal to {max}")
    @Xss(message = "Institution name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String institutionName;
    private Integer graduated;
    private LocalDate startDate;
    private LocalDate endDate;
    @JsonProperty("degree")
    private Long degreeId;
    @Size(max = 200, message = "major length must be less than or equal to {max}")
    @Xss(message = "Major some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String major;
    @Size(max = 200, message = "minor length must be less than or equal to {max}")
    @Xss(message = "Minor some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String minor;
} 