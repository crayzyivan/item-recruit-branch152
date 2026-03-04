package com.item.framework.constant;


import lombok.Getter;

@Getter
public enum SortTypeEnum {
    DESC("DESC"),
    ASC( "ASC");

    private final String type;

    SortTypeEnum(String type){
        this.type = type;
    }
}
