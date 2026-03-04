package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewInfoVO {
    @JsonProperty("interview_id")
    private String interviewId;
    @JsonProperty("interview_name")
    private String interviewName;
    @JsonProperty("scheduled_time")
    private LocalDateTime scheduledTime;
}