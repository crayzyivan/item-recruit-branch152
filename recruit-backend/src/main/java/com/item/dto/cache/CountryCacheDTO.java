package com.item.dto.cache;

import lombok.Data;

/**
 * 地理位置 国家
 */
@Data
public class CountryCacheDTO {
    private Long id;
    private String name;
    private String chineseName;
    private String phonecode;
    private String isoCode;
    private String currency;
    private String spanishName;
    private String japaneseName;
} 