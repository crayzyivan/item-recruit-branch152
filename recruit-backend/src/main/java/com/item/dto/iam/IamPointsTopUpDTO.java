package com.item.dto.iam;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * DTO for IAM points top-up operation
 * @author hua.liu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IamPointsTopUpDTO extends IamUserPointsBaseDTO {

    /**
     * Transaction unique number required
     */
    private String transactionNo;

    /**
     * Top-up amount required
     */
    private BigDecimal amount;

    /**
     * Customer code
     */
    private String customerCode;

    /**
     * Remark for the top-up operation
     */
    private String remark;

    /**
     * Currency code
     */
    private String currencyCode;

    /**
     * companyCode required
     */
    private String companyCode;
} 