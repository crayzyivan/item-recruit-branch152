package com.item.framework.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 申请方式枚举
 * 用于标识候选人申请职位的方式
 *
 * @author system
 * @since 2025-11-13
 */
public enum ApplyMethodEnum {
    /**
     * 平台申请（0）
     * 候选人通过平台主动申请职位
     */
    PLATFORM_APPLY(0, "Platform Apply"),
    
    /**
     * 邀请面试自动投递（1）
     * HR邀请外部候选人面试时自动创建的申请
     */
    INVITE_INTERVIEW_AUTO_APPLY(1, "Invite Interview Auto Apply");

    private final Integer code;
    private final String description;

    ApplyMethodEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    private static final class Holder {
        private static final Map<Integer, ApplyMethodEnum> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(ApplyMethodEnum::getCode, Function.identity()));
    }

    /**
     * 通过 code 获取枚举
     *
     * @param code 申请方式代码
     * @return 申请方式枚举，如果code为null或不存在则返回null
     */
    public static ApplyMethodEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Holder.MAP.get(code);
    }
}

