package com.item.dto.googlemap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Google Map Place Details 结果 DTO
 *
 * @author system
 */
@Data
public class PlaceDetailsResultDTO {
    /**
     * 地址组件列表
     */
    @JsonProperty("address_components")
    private List<AddressComponentDTO> addressComponents;
    
    /**
     * 地点名称
     */
    private String name;
    
    /**
     * 格式化的地址
     */
    @JsonProperty("formatted_address")
    private String formattedAddress;
    
    /**
     * 地理位置信息（包含经纬度）
     */
    private GeometryDTO geometry;
}
