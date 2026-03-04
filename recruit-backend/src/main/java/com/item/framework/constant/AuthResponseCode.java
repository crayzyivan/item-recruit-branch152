package com.item.framework.constant;

/**
 * @author : lh
 */
public enum AuthResponseCode implements IGlobalStatusCode {
    AUTH_NOT(100, "The current user's information was not found."),
    AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT(400, "The identity information of the current user was not found."),

    //不要直接使用msg 需要使用MessageFormat.format处理其中占位符
    AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH(700, "The current user''s identity is not that of {0}."),
    //不要直接使用msg 需要使用MessageFormat.format处理其中占位符
    AUTH_CURRENT_USER_ROLE_NOT_MATCH(800, "The current user''s role is not a {0}."),

    AUTH_CURRENT_USER_NOT_PASS(900, "The current user's identify or role does not allow access."),

    PERMISSION_DENIED(1100, "Permission denied."),

    ONLY_MASTER_CAN_PUBLISH(1200, "Only the master user can publish."),
    ;    

    private final int statusCode;

    private final String message;

    AuthResponseCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public int getCode() {
        return AUTH_MODULE + this.statusCode;
    }

    @Override
    public String getMsg() {
        return this.message;
    }
}
