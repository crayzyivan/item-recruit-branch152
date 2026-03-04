package com.item.framework.constant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : lh
 */
public enum AyrshareHotListStatus {
    /**
     *  0 未配置paiKey 1存在apikey但是不能其中hotlist 由于没有linked到任何平台 2 可以使用hotlist功能
     */
    AYRSHARE_APIKEY_NOT_FOUND(0, "api key not fount"),
    /**
     *
     */
    AYRSHARE_APIKEY_BUT_NOT_LINKED(1, "api key exist but not linked"),
    /**
     */
    AYRSHARE_ENABLE_HOTLIST(2, "enable hotlist"),

    ;

    private final int code;
    private final String desc;

    AyrshareHotListStatus(int code, String desc) {
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
        private static final Map<Integer, AyrshareHotListStatus> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(AyrshareHotListStatus::getCode, Function.identity()));
        private static final List<AyrshareHotListStatus> VALUES = Arrays.stream(values()).collect(Collectors.toUnmodifiableList());
    }

    public static List<AyrshareHotListStatus> getAll() {
        return AyrshareHotListStatus.Holder.VALUES;
    }

    /**
     * 通过 code 获取枚举
     */
    public static AyrshareHotListStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return AyrshareHotListStatus.Holder.MAP.get(code);
    }
}
