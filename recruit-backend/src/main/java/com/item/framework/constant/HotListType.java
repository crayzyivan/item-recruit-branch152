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
public enum HotListType {
    ENABLE(1, "enabled"),
    DISABLE(0, "disabled");;
    private final int code;
    private final String desc;

    HotListType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static final class Holder {
        private static Map<Integer, HotListType> MAP = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(HotListType::getCode, Function.identity()));
    }


    /**
     * 通过 code 获取枚举
     */
    public static HotListType getByCode(Integer code) {
        if (code == null) {
            return DISABLE;
        }
        return HotListType.Holder.MAP.getOrDefault(code, DISABLE);
    }

    public static boolean isEnabled(Integer code) {
        if (code == null) {
            return false;
        }
        return getByCode(code) == ENABLE;
    }
}
