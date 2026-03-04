package com.item.dto.googlemap;

import lombok.Data;

/**
 * Google Map Location Coordinate DTO
 * 地理位置坐标（经纬度）
 *
 * @author system
 */
@Data
public class LocationCoordinateDTO {
    /**
     * 纬度
     */
    private Double lat;
    
    /**
     * 经度
     */
    private Double lng;
}
