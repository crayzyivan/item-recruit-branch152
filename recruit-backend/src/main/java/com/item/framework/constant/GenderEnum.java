package com.item.framework.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public enum GenderEnum {
    MALE(1, "male"),
    FEMALE(2, "female");

    @Getter
    private final Integer code;

    @Getter
    private final String value;

    GenderEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public static Integer getCodeByValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (GenderEnum genderEnum : values()) {
            if (genderEnum.getValue().equals(value.toLowerCase())) {
                return genderEnum.getCode();
            }
        }
        return null;
    }
}
