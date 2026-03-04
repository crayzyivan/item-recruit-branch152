package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 通话分析实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallAnalysisVO {
    @JsonProperty("call_summary")
    private String callSummary;

    @JsonProperty("in_voicemail")
    private boolean inVoicemail;

    @JsonProperty("user_sentiment")
    private String userSentiment;

    @JsonProperty("call_successful")
    private boolean callSuccessful;

    /**
     * Key topics discussed in the call
     */
    @JsonProperty("key_topics")
    private List<String> keyTopics;

}

