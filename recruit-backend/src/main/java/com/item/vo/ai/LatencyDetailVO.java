package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
/**
 * 延迟详情实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LatencyDetailVO {
    @JsonProperty("max")
    private int max;

    @JsonProperty("min")
    private int min;

    @JsonProperty("num")
    private int num;

    @JsonProperty("p50")
    private double p50;

    @JsonProperty("p90")
    private double p90;

    @JsonProperty("p95")
    private double p95;

    @JsonProperty("p99")
    private double p99;

    @JsonProperty("values")
    private List<Integer> values;

}
