package com.item.dto.iam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for IAM points deduction and freezing
 * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412048
 * @author hua.liu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IamPointsMixedConsumeDTO extends IamUserPointsBaseDTO {
    /**
     * 交易流水号 required
     */
    private String transactionNo;

    /**
     * 扣减积分数量 required
     */
    private Long deductedPoints;

    /**
     * 冻结积分数量 required
     */
    private Long freezePoints;

    /**
     * 冻结过期时间（小时）
     */
    private Integer freezeExpireHours;

    /**
     * 操作人 required
     */
    private String operator;

    /**
     * 备注
     */
    private String remark;
} 