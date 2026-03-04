package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
/**
 * 通话成本实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallCostVO {
    @JsonProperty("combined_cost")
    private double combinedCost;

    @JsonProperty("product_costs")
    private List<ProductCostVO> productCosts;

    @JsonProperty("total_duration_seconds")
    private int totalDurationSeconds;

    @JsonProperty("total_duration_unit_price")
    private double totalDurationUnitPrice;

}
