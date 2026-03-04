package com.item.dto.job;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class JobSearchDTO {
    @NotNull
    @Min(1)
    private Integer pageNum;

    @NotNull
    @Min(1)
    private Integer pageSize;

    private String title;
    private String location;
    private String urlCode;
    private String jobDetail;
    private String jobRequirement;
    private String mainDuty;
    private String skills;
} 