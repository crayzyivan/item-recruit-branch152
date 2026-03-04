package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 延迟统计信息实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LatencyVO {
    @JsonProperty("e2e")
    private LatencyDetailVO e2e;

    @JsonProperty("llm")
    private LatencyDetailVO llm;

    @JsonProperty("tts")
    private LatencyDetailVO tts;

}
