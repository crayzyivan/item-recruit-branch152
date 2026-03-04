package com.item.framework.constant;

/**
 * @author : lh
 */
public enum NetResponseCode implements IGlobalStatusCode{
    ARY_SHARE_FAIL(100, "ayr share fail."),

    ;

    private final int statusCode;

    private final String message;

    NetResponseCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public int getCode() {
        return NET_MODULE + this.statusCode;
    }

    @Override
    public String getMsg() {
        return this.message;
    }
}
