package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InterviewResponseDTO {
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("interview_link")
    private String interviewLink;
    @JsonProperty("original_url")
    private String originalUrl;
    @JsonProperty("interview_info")
    private InterviewInfoDTO interviewInfo;

}
