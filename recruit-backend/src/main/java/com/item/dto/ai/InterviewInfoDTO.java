package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewInfoDTO {
    @JsonProperty("interview_id")
    private String interviewId;
    @JsonProperty("interview_name")
    private String interviewName;
    @JsonProperty("scheduled_time")
    private LocalDateTime scheduledTime;
}