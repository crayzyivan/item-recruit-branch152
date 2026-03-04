package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Send SMS Response VO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendSmsResponseVO {
    /**
     * Whether the SMS was sent successfully
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * Message SID
     */
    @JsonProperty("message_sid")
    private String messageSid;

    /**
     * Message status
     */
    @JsonProperty("status")
    private String status;

    /**
     * Recipient phone number
     */
    @JsonProperty("to")
    private String to;
}

