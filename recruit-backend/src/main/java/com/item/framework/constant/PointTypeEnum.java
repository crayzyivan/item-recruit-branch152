package com.item.framework.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分类型
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  14:02
 */
@Getter
@AllArgsConstructor
public enum PointTypeEnum {
    GIFTED(1),//赠送积分
    PAID(2);//充值积分

    private final int code;


}