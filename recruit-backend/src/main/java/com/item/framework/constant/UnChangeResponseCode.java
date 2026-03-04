package com.item.framework.constant;

public enum UnChangeResponseCode implements IGlobalStatusCode {

    INSUFFICIENT_POINTS(2500,"Insufficient points. Please top up and try again."),
    PRIMARY_ACCOUNT_NOT_FOUND(2501, "Primary account not found.");


    private final int statusCode;

    private final String message;

    UnChangeResponseCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public int getCode() {
        return UNCHANGE_MODULE + this.statusCode;
    }

    @Override
    public String getMsg() {
        return this.message;
    }
}
