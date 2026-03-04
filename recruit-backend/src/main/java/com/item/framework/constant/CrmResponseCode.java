package com.item.framework.constant;

/**
 * @author : lh
 */
public enum CrmResponseCode implements IGlobalStatusCode{
    CRM_SUCCESS(100, "crm success."),
    CRM_72000H101_FAIL(200, "crm fail."),
    CRM_FAIL(400, "crm fail."),

    ;

    private final int statusCode;

    private final String message;

    CrmResponseCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
    @Override
    public int getCode() {
        return CRM_MODULE+this.statusCode;
    }

    @Override
    public String getMsg() {
        return this.message;
    }
}
