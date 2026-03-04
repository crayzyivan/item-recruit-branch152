package com.item.framework.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 招聘账号类型枚举
 * 用于识别招聘者账号类型，特别是判定是否为主账号
 * 
 * @author hua.liu
 * @since 1.0.0
 */
public enum RecruitAccountTypeEnum {
    /**
     * undefined - 未定义类型
     */
    UNDEFINED("undefined", "Undefined Recruit Account Type"),

    /**
     * primary - 主账号类型
     */
    PRIMARY("primary", "Primary Recruit Account Type"),

    ;

    @Getter
    private final String code;
    @Getter
    private final String desc;

    RecruitAccountTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static final class Holder {
        private static final Map<String, RecruitAccountTypeEnum> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(RecruitAccountTypeEnum::getCode, Function.identity()));
    }

    /**
     * 通过 code 获取枚举
     */
    public static RecruitAccountTypeEnum getByCode(String code) {
        if (code == null || StringUtils.isBlank(code)) {
            return UNDEFINED;
        }
        return RecruitAccountTypeEnum.Holder.MAP.getOrDefault(code, UNDEFINED);
    }

    /**
     * 检查是否为主账号类型
     */
    public boolean isPrimary() {
        return this == PRIMARY;
    }
}
