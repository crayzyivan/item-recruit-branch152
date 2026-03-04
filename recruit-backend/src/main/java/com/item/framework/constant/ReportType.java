package com.item.framework.constant;

import lombok.Getter;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
@Getter
public enum ReportType {
    LAST7DAYS(0),
    LAST30DAYS(1),
    LAST3MONTHS(2);

    private final int code;

    ReportType(int code) {
        this.code = code;
    }

    public static ReportType getByCode(int code) {
        for (ReportType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ReportType code: " + code);
    }
}
