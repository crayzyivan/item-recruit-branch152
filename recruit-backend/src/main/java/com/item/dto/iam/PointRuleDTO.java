package com.item.dto.iam;

import lombok.Data;

import static com.item.framework.constant.CommonConstants.StrConstants.APP_CODE;

/**
 * 查询积分规则参数
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-31  14:41
 */
@Data
public class PointRuleDTO {

    private String applicationCode = APP_CODE;
    private String currencyCode;
    private Integer status;
    private Integer page;
    private Integer size;

}