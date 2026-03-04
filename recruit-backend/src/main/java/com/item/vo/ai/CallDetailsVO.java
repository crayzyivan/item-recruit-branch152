package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Call details object
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallDetailsVO
{
    /**
     * Call ID
     */
    @JsonProperty("call_id")
    private String callId;

    /**
     * Call duration in seconds
     */
    private Integer duration;

    @JsonProperty("call_cost")
    private CallCostVO callCost;

    /**
     * Disconnect reason
     */
    @JsonProperty("disconnect_reason")
    private String disconnectReason;

    /**
     * Recording URL
     */
    @JsonProperty("recording_url")
    private String recordingUrl;

    /**
     * Transcript text
     */
    private String transcript;

    /**
     * Start timestamp (milliseconds)
     */
    @JsonProperty("start_timestamp")
    private Long startTimestamp;

    /**
     * End timestamp (milliseconds)
     */
    @JsonProperty("end_timestamp")
    private Long endTimestamp;

    /**
     * Call analysis
     */
    @JsonProperty("call_analysis")
    private CallAnalysisVO callAnalysis;

    /**
     * Transcript object
     */
    @JsonProperty("transcript_object")
    private List<TranscriptObjectVO> transcriptObject;
}

