package com.item.dto.googlemap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Google Map Autocomplete 预测结果 DTO
 * 
 * @author haibin.bian
 * @since 2026-02-05
 */
@Data
public class PredictionDTO {
    /**
     * Google Map Place ID
     */
    @JsonProperty("place_id")
    private String placeId;
    
    /**
     * 地点描述（完整地址）
     */
    @JsonProperty("description")
    private String description;
}
