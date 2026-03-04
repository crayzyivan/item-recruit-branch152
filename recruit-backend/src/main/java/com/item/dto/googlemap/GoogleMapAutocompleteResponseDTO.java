package com.item.dto.googlemap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Google Map Autocomplete API 响应 DTO
 * 
 * @author haibin.bian
 * @since 2026-02-05
 */
@Data
public class GoogleMapAutocompleteResponseDTO {
    /**
     * 预测结果列表
     */
    @JsonProperty("predictions")
    private List<PredictionDTO> predictions;
    
    /**
     * 响应状态
     * OK - 成功
     * ZERO_RESULTS - 无结果
     * OVER_QUERY_LIMIT - 超过查询限制
     * REQUEST_DENIED - 请求被拒绝
     * INVALID_REQUEST - 无效请求
     */
    @JsonProperty("status")
    private String status;
}
