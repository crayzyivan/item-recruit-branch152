package com.item.dto.iam;

import static com.item.framework.constant.CommonConstants.StrConstants.APP_CODE;
import lombok.Data;

/**
 * DTO for IAM points transfer
 * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412046
 * @author hua.liu
 */
@Data
public class IamPointsTransferDTO {
    /**
     * 转出用户ID required
     */
    private Long fromUserId;

    /**
     * 转入用户ID required
     */
    private Long toUserId;

    /**
     * 交易流水号 required
     */
    private String transactionNo;

    /**
     * 应用编码 required
     */
    private String appCode = APP_CODE;

    /**
     * 积分数量 required
     */
    private Long points;

    /**
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