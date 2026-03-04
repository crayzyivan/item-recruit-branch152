package com.item.dto.googlemap;

import lombok.Data;

/**
 * Google Map Geometry DTO
 * 包含地理位置的经纬度信息
 *
 * @author system
 */
@Data
public class GeometryDTO {
    /**
     * 地理位置坐标
     */
    private LocationCoordinateDTO location;
}
