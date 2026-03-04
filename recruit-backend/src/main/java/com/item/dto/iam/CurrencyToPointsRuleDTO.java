package com.item.dto.iam;

import lombok.Data;


/**
 * 积分规则
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-31  15:19
 */
@Data
public class CurrencyToPointsRuleDTO {
    private Long id;
    private String applicationCode;
    private String currencyCode;
    private Integer moneyToPoints;
    private Integer minimumAmount;
    private Integer status;
    private String description;
    private String createdBy;
    private String updatedBy;
}