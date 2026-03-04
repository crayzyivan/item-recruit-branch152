package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transcript item DTO for JSON conversion
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptItemDTO {
    /**
     * Spokesperson name (Agent or User)
     */
    @JsonProperty("spokesperson")
    private String spokesperson;

    /**
     * Time range (start - end)
     */
    @JsonProperty("time")
    private String time;

    /**
     * Content text
     */
    @JsonProperty("content")
    private String content;
}

