package com.item.framework.constant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ayrshare分享状态枚举
 * 
 * @author system
 * @since 1.0.0
 */
public enum AyrshareStatus {
    /**
     * 0 - 不需要分享
     */
    NO_SHARE(0, "No Share"),
    
    /**
     * 1 - 分享中
     */
    SHARING(1, "Sharing"),
    
    /**
     * 2 - 分享成功
     */
    SHARE_SUCCESS(2, "Share Success"),
    
    /**
     * 3 - 部分成功
     */
    PARTIAL_SUCCESS(3, "Partial Success"),
    
    /**
     * -1 - 分享失败
     */
    SHARE_FAILED(-1, "Share Failed"),
    
    ;

    private final Integer code;
    private final String description;

    AyrshareStatus(Integer code, String description) {
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
        private static final Map<Integer, AyrshareStatus> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(AyrshareStatus::getCode, Function.identity()));
        private static final List<AyrshareStatus> VALUES = Arrays.stream(values()).collect(Collectors.toUnmodifiableList());
    }

    public static List<AyrshareStatus> getAll() {
        return AyrshareStatus.Holder.VALUES;
    }

    /**
     * 通过 code 获取枚举
     */
    public static AyrshareStatus getByCode(Integer code) {
        if (code == null) {
            return NO_SHARE;
        }
        return AyrshareStatus.Holder.MAP.get(code);
    }

    /**
     * 检查是否为成功状态
     */
    public boolean isSuccess() {
        return this == SHARE_SUCCESS || this == PARTIAL_SUCCESS;
    }

    /**
     * 检查是否为失败状态
     */
    public boolean isFailed() {
        return this == SHARE_FAILED;
    }

    /**
     * 检查是否为进行中状态
     */
    public boolean isInProgress() {
        return this == SHARING;
    }

    /**
     * 检查是否为终止状态（成功或失败）
     */
    public boolean isTerminal() {
        return isSuccess() || isFailed();
    }
}