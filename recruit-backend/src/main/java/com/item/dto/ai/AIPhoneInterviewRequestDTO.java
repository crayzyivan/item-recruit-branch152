package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * AI Phone Interview Request DTO
 * Used for scheduling phone interviews with external AI interviewer service
 * 
 * @author AI Phone Interview Module
 */
@Data
public class AIPhoneInterviewRequestDTO {

    /**
     * Interview ID
     */
    @JsonProperty("interview_id")
    private String interviewId;

    /**
     * Country
     */
    @JsonProperty("language")
    private String language;

    /**
     * Candidate phone number (with country code)
     * Example: +18886886909
     */
    @JsonProperty("candidate_phone")
    private String candidatePhone;

    /**
     * Candidate name
     */
    @JsonProperty("candidate_name")
    private String candidateName;

    /**
     * Candidate email
     */
    @JsonProperty("candidate_email")
    private String candidateEmail;

    /**
     * Scheduled interview time
     * Format: ISO 8601 with timezone (e.g., 2025-12-11T17:12:00+08:00)
     */
    @JsonProperty("scheduled_time")
    private String scheduledTime;

    /**
     * Application ID
     */
    @JsonProperty("application_id")
    private String applicationId;
}
