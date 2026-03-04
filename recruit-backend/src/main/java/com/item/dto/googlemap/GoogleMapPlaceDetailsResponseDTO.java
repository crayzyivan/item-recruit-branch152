package com.item.dto.googlemap;

import lombok.Data;

/**
 * Google Map Place Details API 响应 DTO
 *
 * @author system
 */
@Data
public class GoogleMapPlaceDetailsResponseDTO {
    /**
     * 地点详情结果
     */
    private PlaceDetailsResultDTO result;
    
    /**
     * API 响应状态
     * OK: 成功
     * ZERO_RESULTS: 无结果
     * OVER_QUERY_LIMIT: 超过查询限制
     * REQUEST_DENIED: 请求被拒绝
     * INVALID_REQUEST: 无效请求
     * UNKNOWN_ERROR: 未知错误
     */
    private String status;
}
