package com.item.framework.constant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 薪资类型枚举
 * 被字典表代替
 */
@Deprecated
public enum SalaryType {
    /**
     * 按小时
     */
    HOURLY(1, "Hourly"),

    /**
     * 按天
     */
    DAILY(2, "Daily"),

    /**
     * 按周
     */
    WEEKLY(3, "Weekly"),

    /**
     * 按月
     */
    MONTHLY(4, "Monthly"),

    /**
     * 按年
     */
    ANNUAL(5, "Annual"),

    /**
     * 按提成
     */
    COMMISSION(6, "Commission"),

    /**
     * 其他
     */
    OTHER(7, "Other");

    private final int code;
    private final String desc;

    SalaryType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    private static final class Holder {
        private static Map<Integer, SalaryType> MAP = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(SalaryType::getCode, Function.identity()));
        private static List<SalaryType> VALUES = Arrays.stream(values()).collect(Collectors.toUnmodifiableList());
    }

    public static List<SalaryType> getAll() {
        return Holder.VALUES;
    }

    /**
     * 通过 code 获取枚举
     */
    public static SalaryType getByCode(Integer code) {
        if (code == null) {
            return OTHER;
        }
        return Holder.MAP.getOrDefault(code, OTHER);
    }
} 