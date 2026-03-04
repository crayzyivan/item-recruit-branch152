package com.item.vo;

import com.item.dto.CurrencyTypeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * CurrencyType VO
 *
 * @author system
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyTypeVO implements Serializable {
    private Integer id;

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

    public static List<CurrencyTypeVO> convert2VO(List<CurrencyTypeDTO> currencyTypes) {
        return currencyTypes.stream().map(type -> new CurrencyTypeVO(type.getId(), type.getSimpleDescription(), type.getDescription(), type.getSymbol())).toList();
    }
} 