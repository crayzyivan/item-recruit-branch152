package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI callback VO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiCallbackVO
{
    /**
     * Callback event type
     */
    @NotBlank(message = "The event cannot be empty")
    private String event;

    /**
     * 事件AI处理完成时间
     */
    private LocalDateTime timestamp;

    /**
     * AI面试ID
     */
//    @NotBlank(message = "The interviewId cannot be empty")
    @JsonProperty("interview_id")
    private String interviewId;

    /**
     * 面试者邮箱
     */
//    @NotBlank(message = "The interviewerEmail cannot be empty")
    @Email(message = "The interviewerEmail format is incorrect")
    @JsonProperty("interviewer_email")
    private String interviewerEmail;

    /**
     * 面试者面试ID
     */
//    @NotBlank(message = "The callId cannot be empty")
    @JsonProperty("call_id")
    private String callId;

    /**
     * 应聘者职位关联ID，对应r_candidate_job表的主键id
     * 用于确定具体是哪个账号参加的面试
     */
    @JsonProperty("application_id")
    private Long applicationId;

    /**
     * Callback data
     */
    @JsonProperty("data")
    private AiCallbackDataVO data;
}
