package com.item.framework.constant;

/**
 * @author : lh
 */
public enum FeignClientResponseCode implements IGlobalStatusCode {
    FEIGN_CLIENT_FAIL(100, "feign client fail."),
    IAM_CREATE_USER_FAIL(300, "Iam create user fail."),
    IAM_HAVE_NOT_RESPONSE(400, "Iam have not response."),
    ;

    private final int statusCode;

    private final String message;

    FeignClientResponseCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public int getCode() {
        return FEIGN_MODULE + this.statusCode;
    }

    @Override
    public String getMsg() {
        return this.message;
    }
}
