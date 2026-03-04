package com.item.framework.constant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 币种类型枚举  被字典表代替了
 */
@Deprecated
public enum CurrencyType {
    /**
     * 美元
     */
    USD(1, "USD", "US Dollar"),
    /**
     * 英镑
     */
    GBP(2, "GBP", "British Pound"),
    /**
     * 欧元
     */
    EUR(3, "EUR", "Euro"),
    /**
     * 加元
     */
    CAD(4, "CAD", "Canadian Dollar"),
    /**
     * 人名币
     */
    CNY(5, "CNY", "Chinese Yuan"),
    /**
     * 菲律宾比索
     */
    PHP(6, "PHP", "Philippine Peso"),
    ;

    private final int code;
    private final String simpleDescription;
    private final String desc;

    CurrencyType(int code, String simpleDescription, String desc) {
        this.code = code;
        this.simpleDescription = simpleDescription;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getSimpleDescription() {
        return simpleDescription;
    }

    public String getDesc() {
        return desc;
    }

    private static final class Holder {
        private static final Map<Integer, CurrencyType> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(CurrencyType::getCode, Function.identity()));
        private static final List<CurrencyType> VALUES = Arrays.stream(values()).collect(Collectors.toUnmodifiableList());
    }

    public static List<CurrencyType> getAll() {
        return Holder.VALUES;
    }

    /**
     * 通过 code 获取枚举
     */
    public static CurrencyType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Holder.MAP.get(code);
    }
} 