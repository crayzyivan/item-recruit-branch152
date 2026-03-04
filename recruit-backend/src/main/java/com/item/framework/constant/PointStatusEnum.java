package com.item.framework.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分状态 状态(0正常;1预占;2已取消;3失败)
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  14:02
 */
@Getter
@AllArgsConstructor
public enum PointStatusEnum {
    NORMAL(0),//正常
    FREEZE(1),//冻结
    CANCELED(2),//已取消
    ERROR(3);//失败

    private final int code;

}