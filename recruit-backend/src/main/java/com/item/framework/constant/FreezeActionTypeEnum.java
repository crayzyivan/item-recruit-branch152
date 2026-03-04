package com.item.framework.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预占意图(1待确认;2待取消)
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-31  14:02
 */
@Getter
@AllArgsConstructor
public enum FreezeActionTypeEnum {
    CONFIRM(1),//确认扣除冻结积分
    CAMCEL(2);//取消冻结积分

    private final int code;

}