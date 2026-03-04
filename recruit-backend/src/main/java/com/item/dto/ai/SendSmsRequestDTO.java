package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Send SMS Request DTO
 * Used for sending SMS to candidates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendSmsRequestDTO {

    @JsonProperty("from_phone")
    private String from;
    /**
     * Recipient phone number (with country code)
     * Example: +8613812345678
     */
    @JsonProperty("to_phone")
    private String to;

    /**
     * SMS message body
     */
    @JsonProperty("message")
    private String body;
}

