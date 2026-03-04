package com.item.dto.googlemap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Google Map 地址组件 DTO
 *
 * @author system
 */
@Data
public class AddressComponentDTO {
    /**
     * 长名称
     */
    @JsonProperty("long_name")
    private String longName;
    
    /**
     * 短名称
     */
    @JsonProperty("short_name")
    private String shortName;
    
    /**
     * 类型列表
     * 例如: ["street_number"], ["route"], ["locality", "political"]
     */
    private List<String> types;
}
