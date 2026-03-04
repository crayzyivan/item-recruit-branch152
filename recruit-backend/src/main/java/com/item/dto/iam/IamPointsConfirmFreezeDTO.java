package com.item.dto.iam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for confirming the consumption of frozen points
 * @author hua.liu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IamPointsConfirmFreezeDTO extends IamUserPointsBaseDTO {

    /**
     * Transaction unique number required
     */
    private String transactionNo;

    /**
     * Operator who confirms the consumption
     */
    private String operator;

    /**
     * Remark for confirming the consumption of frozen points
     */
    private String confirmRemark;
} 