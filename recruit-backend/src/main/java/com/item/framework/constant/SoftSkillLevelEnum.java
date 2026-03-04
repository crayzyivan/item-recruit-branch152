package com.item.framework.constant;

import lombok.Getter;

/**
 * ai面试等级枚举
 */
@Getter
public enum SoftSkillLevelEnum {
    A1(10,"A1"),
    A2(20,"A2"),
    B1(30,"B1"),
    B2(40,"B2"),
    C1(50,"C1"),
    C2(60,"C2");

    private final int level;
    private final String name;
    SoftSkillLevelEnum(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public static String getNameByCode(int code) {
        for (SoftSkillLevelEnum level : values()) {
            if (level.getLevel() == code) {
                return level.getName();
            }
        }
        return null;
    }
}
