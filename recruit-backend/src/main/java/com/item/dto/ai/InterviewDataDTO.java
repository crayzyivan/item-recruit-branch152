package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/19
 * @since 1.0.0
 */
@Data
public class InterviewDataDTO implements Serializable {
    @JsonProperty(value = "user_id")
    private String userId;
    @JsonProperty(value = "organization_id")
    private String organizationId;
    @JsonProperty(value = "interviewer_id")
    private String interviewerId;
    @JsonProperty(value = "is_anonymous")
    private Boolean isAnonymous;
    private String description;
    @JsonProperty("logo_url")
    private String logoUrl;
    private String name;
    @JsonProperty("language")
    private String objective;
    @JsonProperty("response_count")
    private int responseCount;
    @JsonProperty("time_duration")
    private int timeDuration;
    @JsonProperty(value = "can_change_interview_language")
    private Boolean canChangeInterviewLanguage;
    @JsonProperty("enable_written_test")
    private Boolean enableWrittenTest;
    /**
     * 面试类型 (0:video, 1:audio)
     */
    @JsonProperty("interview_type")
    private String interviewType;
}
