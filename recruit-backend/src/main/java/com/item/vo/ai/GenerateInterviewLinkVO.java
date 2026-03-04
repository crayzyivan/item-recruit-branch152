package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GenerateInterviewLinkVO {
    @JsonProperty("interview_id")
    private String interviewid;
    @JsonProperty("candidate_email")
    private String candidateEmail;
    @JsonProperty("scheduled_time")
    private LocalDateTime scheduledTime;

}
