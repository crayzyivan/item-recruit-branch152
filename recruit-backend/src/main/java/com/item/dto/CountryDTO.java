package com.item.dto;

import lombok.Data;

/**
 * 国家
 */
@Data
public class CountryDTO {
    private Long id;
    private String name;
    private String phonecode;
    private String isoCode;
    private String currency;
}