package com.item.framework.error;

import lombok.Getter;

@Getter
public class LoginException extends RuntimeException {
    private int code;

    public LoginException(int code, String message) {
        super(message);
        this.code = code;
    }

}
