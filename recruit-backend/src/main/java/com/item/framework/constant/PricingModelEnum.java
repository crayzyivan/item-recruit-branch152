package com.item.framework.constant;

import lombok.Getter;

/**
 * 计费模式
 */
@Getter
public enum PricingModelEnum{
    MINUTE(1, "Minute Based"),
    TOKEN(2, "Token Based"),
    ;

    private final int code;

    private final String name;

    PricingModelEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

}
