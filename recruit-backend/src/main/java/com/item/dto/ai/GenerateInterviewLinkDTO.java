package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GenerateInterviewLinkDTO {
    @JsonProperty("interview_id")
    private String interviewid;
    @JsonProperty("candidate_email")
    private String candidateEmail;
    @JsonProperty("candidate_name")
    private String candidateName;
    @JsonProperty("scheduled_time")
    private LocalDateTime scheduledTime;
    
    /**
     * 应聘者职位关联ID，对应r_candidate_job表的主键id
     * 用于AI服务准确识别具体的应聘者账号
     */
    @JsonProperty("application_id")
    private Long applicationId;
    @JsonProperty("location")
    private String location;
}
