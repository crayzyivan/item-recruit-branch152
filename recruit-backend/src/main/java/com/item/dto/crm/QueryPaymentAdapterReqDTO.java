package com.item.dto.crm;

import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class QueryPaymentAdapterReqDTO {
    private String searchValue;
    /**
     * Allowed values: 1 3
     */
    private Integer paymentType;

    /**
     * Allowed values: 101 102 301 302 303
     */
    private List<Integer> subType;

    private Integer pageIndex;
    private Integer pageSize;

    private String customerCodeOrId;

}
