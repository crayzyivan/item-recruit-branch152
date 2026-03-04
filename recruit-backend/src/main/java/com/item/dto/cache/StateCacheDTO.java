package com.item.dto.cache;

import lombok.Data;

/**
 * 地理位置 省
 */
@Data
public class StateCacheDTO {
    private Long id;
    private String name;
    private Long countryId;
    private String chineseName;
    private String spanishName;
    private String japaneseName;
} 