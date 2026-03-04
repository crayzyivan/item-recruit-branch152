package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 产品成本实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductCostVO {
    @JsonProperty("cost")
    private double cost;

    @JsonProperty("product")
    private String product;

    @JsonProperty("unit_price")
    private double unitPrice;

}
