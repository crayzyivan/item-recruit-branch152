package com.item.framework.error;

import com.item.framework.constant.IGlobalStatusCode;
import lombok.Getter;

/**
 * @author : lh
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(IGlobalStatusCode globalStatusCode) {
        super(globalStatusCode.getMsg());
        this.code = globalStatusCode.getCode();
        this.message = globalStatusCode.getMsg();
    }

    public static BusinessException of(IGlobalStatusCode globalStatusCode) {
        return new BusinessException(globalStatusCode);
    }

    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }
}
