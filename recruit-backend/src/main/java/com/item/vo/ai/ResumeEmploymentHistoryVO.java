package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeEmploymentHistoryVO {
    private String companyName;
    private String currentEmployer;
    private String jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    @JsonProperty("responsibilities")
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private String keyResponsibilities;
} 