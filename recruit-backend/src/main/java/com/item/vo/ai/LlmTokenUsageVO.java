package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
/**
 * LLM令牌使用统计实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmTokenUsageVO {
    @JsonProperty("values")
    private List<Integer> values;

    @JsonProperty("average")
    private int average;

    @JsonProperty("num_requests")
    private int numRequests;

}
