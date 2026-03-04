package com.item.dto.iam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for canceling frozen points
 * @author hua.liu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IamPointsCancelFreezeDTO extends IamUserPointsBaseDTO {

    /**
     * Transaction unique number required
     */
    private String transactionNo;

    /**
     * Operator who cancels the freeze
     */
    private String operator;

    /**
     * Remark for canceling the freeze
     */
    private String cancelRemark;
} 