package com.item.framework.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 招聘申请状态 枚举
 *
 * @author lh
 * @version 1.0
 * @since 2025-07-29
 */
public enum CrmLeadsCustomerStatus {

    /**
     * 转换状态1 初始创建leads， 2 leads转customer完成
     */
    INIT(1, "init"),
    CONVERT_FINISH(2, "convert finish"),
    CONVERT_ERROR(3, "convert error"),
    ;

    private final int code;
    private final String name;

    CrmLeadsCustomerStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static final class Holder{
        private static final Map<Integer, CrmLeadsCustomerStatus> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(CrmLeadsCustomerStatus::getCode, Function.identity()));
    }

    public static CrmLeadsCustomerStatus getByCode(Integer code) {
        if (code == null) {
            return INIT;
        }
        return CrmLeadsCustomerStatus.Holder.MAP.get(code);
    }
}
