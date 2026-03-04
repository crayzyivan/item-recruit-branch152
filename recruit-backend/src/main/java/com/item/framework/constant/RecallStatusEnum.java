package com.item.framework.constant;

import lombok.Getter;

@Getter
public enum RecallStatusEnum {

    UNRECALLED(0, "unrecalled"),
    RECALLED(1, "recalled"),
    ;


    private final int code;
    private final String desc;

    RecallStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
