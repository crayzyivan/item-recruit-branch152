package com.item.framework.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : lh
 */
@Getter
public enum UserViewEnum {
    NOTHING_VIEW(0, "NOTHING View", "Nothing View"),
    ALL_VIEW(1, "All View", "All View"),
    XML_SETTING_VIEW(2, "XML Setting", "Xml Setting View"),
    ;
    private final int code;
    private final String name;
    private final String desc;

    UserViewEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    private static final class Holder {
        private static final Map<Integer, UserViewEnum> MAP = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(UserViewEnum::getCode, Function.identity()));
    }

    public static UserViewEnum getByCode(int code) {
        return Holder.MAP.getOrDefault(code, UserViewEnum.NOTHING_VIEW);
    }
}
