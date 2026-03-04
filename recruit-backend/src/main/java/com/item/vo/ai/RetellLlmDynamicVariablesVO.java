package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * LLM动态变量实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetellLlmDynamicVariablesVO {
    @JsonProperty("mins")
    private String mins;

    @JsonProperty("name")
    private String name;

    @JsonProperty("objective")
    private String objective;

    @JsonProperty("questions")
    private String questions;
}
