package com.item.framework.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工作模式类型枚举（如：现场、远程、混合、其他）
 */
@Deprecated
public enum LocationType {
    /** 现场 */
    ON_SITE(1, "On-site"),
    /** 远程 */
    REMOTE(2, "Remote"),
    /** 混合 */
    HYBRID(3, "Hybrid"),
    /** 其他 */
    OTHER(4, "Other");

    /** 编码，从1开始 */
    private final int code;
    /** 英文描述 */
    private final String desc;

    LocationType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取英文描述
     */
    public String getDesc() {
        return desc;
    }

    private static final class Holder {
        private static final Map<Integer, LocationType> MAP = Arrays.stream(values())
                .collect(Collectors.toConcurrentMap(LocationType::getCode, Function.identity()));
    }

    /**
     * 通过 code 获取枚举
     */
    public static LocationType getByCode(Integer code) {
        if (code == null) {
            return OTHER;
        }
        return Holder.MAP.getOrDefault(code, OTHER);
    }
} 