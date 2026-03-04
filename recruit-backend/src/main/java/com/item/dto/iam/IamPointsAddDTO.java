package com.item.dto.iam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for IAM points reward
 * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412054
 *
 * @author hua.liu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IamPointsAddDTO extends IamUserPointsBaseDTO {

    /**
     * 交易流水号 required
     */
    private String transactionNo;

    /**
     * 积分数量 required
     */
    private Long points;

    /**
     * Point type (1-gifted points, 2-paid points)
     * 积分类型 required
     */
    private Integer pointType;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 备注
     */
    private String remark;
} 