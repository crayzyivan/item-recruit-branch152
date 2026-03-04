package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewTypeDataResultVO {
    @JsonProperty("call_id")
    private String callId;
    /**
     * audio/video
     */
    @JsonProperty("interview_type")
    private String interviewType;
}
