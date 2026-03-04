package com.item.framework.constant;

import lombok.Getter;

/**
 * @author : lh
 */
@Getter
public enum CrmModuleEnum {
    CONTACT(2, "Contact"),
    COMPANY(3, "Company"),
    DEAL(4, "Deal"),

    ;
    private final int module;

    private final String description;

    CrmModuleEnum(int module, String description) {
        this.module = module;
        this.description = description;
    }
}
