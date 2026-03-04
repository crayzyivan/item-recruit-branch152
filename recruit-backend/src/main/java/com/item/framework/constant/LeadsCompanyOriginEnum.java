package com.item.framework.constant;

import lombok.Getter;

/**
 * @author : lh
 */
@Getter
public enum LeadsCompanyOriginEnum {
    SIGN_UP(1, "sign up"),
    ADD_PAYMENT(2, "add payment"),

    ;
    private final int origin;

    private final String description;

    LeadsCompanyOriginEnum(int origin, String description) {
        this.origin = origin;
        this.description = description;
    }
}
