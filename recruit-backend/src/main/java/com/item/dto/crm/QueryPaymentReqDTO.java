package com.item.dto.crm;

import com.item.framework.annotation.AllowedInteger;
import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class QueryPaymentReqDTO {
    private String searchValue;
    /**
     * Allowed values: 1 3
     */
    @AllowedInteger(values = {1, 3}, message = "paymentType must be one of the allowed values: {values}")
    private Integer paymentType;

    /**
     * Allowed values: 101 102 301 302 303
     */
    @AllowedInteger(values = {101, 102, 301, 302, 303}, message = "subType must be one of the allowed values: {values}")
    private List<Integer> subType;

    private Integer pageIndex;
    private Integer pageSize;

}
