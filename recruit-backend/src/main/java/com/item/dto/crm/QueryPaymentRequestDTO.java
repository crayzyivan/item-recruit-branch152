package com.item.dto.crm;

import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class QueryPaymentRequestDTO {
    private String SearchValue;
    /**
     * Allowed values: 1 3
     */
    private Integer PaymentType;

    /**
     * Allowed values: 101 102 301 302 303
     */
    private List<Integer> SubType;

    private Integer PageIndex;
    private Integer PageSize;

}
