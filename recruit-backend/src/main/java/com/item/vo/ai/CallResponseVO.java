package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
/**
 * 通话响应主实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallResponseVO {
    @JsonProperty("call_id")
    private String callId;

    @JsonProperty("latency")
    private LatencyVO latency;

    @JsonProperty("agent_id")
    private String agentId;

    @JsonProperty("call_cost")
    private CallCostVO callCost;

    @JsonProperty("call_type")
    private String callType;

    @JsonProperty("agent_name")
    private String agentName;

    @JsonProperty("transcript")
    private String transcript;

    @JsonProperty("call_status")
    private String callStatus;

    @JsonProperty("duration_ms")
    private long durationMs;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("agent_version")
    private int agentVersion;

    @JsonProperty("call_analysis")
    private CallAnalysisVO callAnalysis;

    @JsonProperty("end_timestamp")
    private long endTimestamp;

    @JsonProperty("recording_url")
    private String recordingUrl;

    @JsonProperty("public_log_url")
    private String publicLogUrl;

    @JsonProperty("llm_token_usage")
    private LlmTokenUsageVO llmTokenUsage;

    @JsonProperty("start_timestamp")
    private long startTimestamp;

    @JsonProperty("opt_in_signed_url")
    private boolean optInSignedUrl;

    @JsonProperty("transcript_object")
    private List<TranscriptObjectVO> transcriptObject;

    @JsonProperty("disconnection_reason")
    private String disconnectionReason;

    @JsonProperty("transcript_with_tool_calls")
    private List<TranscriptWithToolCallVO> transcriptWithToolCalls;

    @JsonProperty("retell_llm_dynamic_variables")
    private RetellLlmDynamicVariablesVO retellLlmDynamicVariables;

    @JsonProperty("opt_out_sensitive_data_storage")
    private boolean optOutSensitiveDataStorage;

    
}
