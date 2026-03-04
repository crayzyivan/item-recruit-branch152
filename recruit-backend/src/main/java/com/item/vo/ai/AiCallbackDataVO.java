package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI callback data object
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiCallbackDataVO
{
    /**
     * Schedule ID
     */
    @JsonProperty("schedule_id")
    private String scheduleId;

    /**
     * Interview ID
     */
    @JsonProperty("interview_id")
    private String interviewId;

    /**
     * Application ID
     */
    @JsonProperty("application_id")
    private String applicationId;

    /**
     * Candidate name
     */
    private String name;

    /**
     * Candidate email
     */
    @Email(message = "The email format is incorrect")
    private String email;

    /**
     * Candidate phone number
     */
    @JsonProperty("candidate_phone")
    private String candidatePhone;

    /**
     * Interviewer phone number
     */
    @JsonProperty("from_number")
    private String fromPhone;

    /**
     * Call ID
     */
    @JsonProperty("call_id")
    private String callId;

    /**
     * Batch call ID
     */
    @JsonProperty("batch_call_id")
    private String batchCallId;

    @JsonProperty("candidate_job_id")
    private Long candidateJobId;

    private LocalDateTime timestamp;

    /**
     * Call details
     */
    private CallDetailsVO details;

    /**
     * Analytics data
     */
    private AnalyticsVO analytics;
}

