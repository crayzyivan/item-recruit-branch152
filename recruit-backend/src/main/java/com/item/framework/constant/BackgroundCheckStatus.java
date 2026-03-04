package com.item.framework.constant;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
public enum BackgroundCheckStatus {
    INITIATED(0),
    COMPLETED(1),
    FAILED(2);

    private int code;

    BackgroundCheckStatus(int code) {
        this.code = code;
    }

    public  int getCode() {
        return code;
    }

}
