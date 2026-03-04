package com.item.framework.constant;

import lombok.Getter;

/**
 * @author : 数据来源
 */
@Getter
public enum DataSourceEnum {
    RECRUIT(0,"recruit"),//recruit
    PHL(1,"PHL")//菲律宾
    ;
    private final int source;
    private String name;

    DataSourceEnum(int source,String  name) {
        this.source = source;
        this.name = name;
    }
}
