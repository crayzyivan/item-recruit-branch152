package com.item.framework.constant;

import lombok.Getter;

@Getter
public enum SmsSendStatusEnum {

    FAIL(0, "fail"),
    SUCCESS(1, "success"),
    ;


    private final int code;
    private final String desc;

    SmsSendStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
