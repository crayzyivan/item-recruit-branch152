package com.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * CurrencyType DTO
 * 
 * @author system
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyTypeDTO implements Serializable {
    private int id;
    /**
     * Currency type code
     */
    private Integer code;
    
    /**
     * Currency code (e.g., USD, EUR)
     */
    private String simpleDescription;
    
    /**
     * Currency description
     */
    private String description;

    /**
     *
     * symbol $ ￥ 等
     */
    private String symbol;

    public static CurrencyTypeDTO convertFrom(DictionaryDTO currencyType) {
        return new CurrencyTypeDTO(currencyType.getId().intValue(), currencyType.getId().intValue(), currencyType.getCode(), currencyType.getRemark(), currencyType.getValue());
    }
} 