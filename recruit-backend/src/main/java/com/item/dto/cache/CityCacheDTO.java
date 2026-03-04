package com.item.dto.cache;

import lombok.Data;

/**
 * 地理位置 市
 */
@Data
public class CityCacheDTO {
    private Long id;
    private String name;
    private Long stateId;
    private Long countryId;
    private String chineseName;
    private String spanishName;
    private String japaneseName;
} 