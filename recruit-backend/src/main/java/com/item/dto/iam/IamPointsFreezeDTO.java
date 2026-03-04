package com.item.dto.iam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for freezing IAM points
 * @author hua.liu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IamPointsFreezeDTO extends IamUserPointsBaseDTO {

    /**
     * Transaction unique key required
     */
    private String transactionNo;

    /**
     * Points to freeze required
     */
    private Integer points;

    /**
     * Expiration hours required
     */
    private Integer expireHours;

    /**
     * Operator who initiates the freeze
     */
    private String operator;

    /**
     * Remark for the freeze operation
     */
    private String remark;
} 