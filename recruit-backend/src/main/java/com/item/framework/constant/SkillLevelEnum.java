package com.item.framework.constant;

import lombok.Getter;

/**
 * ai面试等级枚举
 */
@Getter
public enum SkillLevelEnum {
    NOT_EXPERIENCED(0,"Not experienced"),
    JUNIOR(10,"Junior"),
    MID_LEVEL(20,"Mid-level"),
    SENIOR(30,"Senior");

    private final int level;
    private final String name;
    SkillLevelEnum(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public static String getNameByCode(int code) {
        for (SkillLevelEnum level : values()) {
            if (level.getLevel() == code) {
                return level.getName();
            }
        }
        return null;
    }
}
