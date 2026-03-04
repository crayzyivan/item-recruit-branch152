package com.item.framework.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 账户类型枚举
 * 用于识别不同类型的用户账户，特别是C端个人用户
 * 
 * @author hua.liu
 * @since 1.0.0
 */
public enum AccountTypeEnum {
    /**
     * none - 未定义 未找到
     */
    NONE_FOUND("none-found", "none-found Account"),

    /**
     * personal - 个人账户（C端用户）
     */
    PERSONAL("personal", "Personal Account"),

    ;

    @Getter
    private final String code;
    @Getter
    private final String desc;

    AccountTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static final class Holder {
        private static final Map<String, AccountTypeEnum> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(AccountTypeEnum::getCode, Function.identity()));
    }

    /**
     * 通过 code 获取枚举
     */
    public static AccountTypeEnum getByCode(String code) {
        if (code == null || StringUtils.isBlank(code)) {
            return NONE_FOUND;
        }
        return AccountTypeEnum.Holder.MAP.getOrDefault(code, NONE_FOUND);
    }

    /**
     * 检查是否为个人账户（C端用户）
     */
    public boolean isPersonal() {
        return this == PERSONAL;
    }
}
