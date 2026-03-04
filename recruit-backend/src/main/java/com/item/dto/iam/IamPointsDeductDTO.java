package com.item.dto.iam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for IAM points deduction operation
 * @author hua.liu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IamPointsDeductDTO extends IamUserPointsBaseDTO {

    /**
     * Transaction unique number required
     */
    private String transactionNo;

    /**
     * Points to deduct required
     */
    private Integer points;

    /**
     * Operator who initiates the deduction
     */
    private String operator;

    /**
     * Remark for the deduction operation
     */
    private String remark;
} 