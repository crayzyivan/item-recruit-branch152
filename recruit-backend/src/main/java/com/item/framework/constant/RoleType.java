package com.item.framework.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/14
 * @since 1.0.0
 */
@Getter
public enum RoleType {
    /**
     * 无需角色
     */
    NONE(0, "None", "None", "None"),
    /**
     * 候选人角色
     */
    CANDIDATE(1, "candidate", "Candidate", "Candidate"),
    /**
     * 主账户角色
     */
    MASTER_USER(2, "masterUser", "Recruit", "Master Account"),
    /**
     * 子账户角色
     */
    SUB_USER(3, "subUser", "Recruit", "Sub Account"),
    ;

    private final int value;
    private final String name;
    //身份 招聘者 应聘者
    private final String identity;

    //角色 主账号 子账号 应聘者
    private final String role;



    RoleType(int value, String name,  String identity, String role) {
        this.value = value;
        this.name = name;
        this.identity = identity;
        this.role = role;
    }

    private static Map<String, RoleType> roleMap = new HashMap<>();
    static {
        for (RoleType roleType : RoleType.values()) {
            roleMap.put(roleType.name, roleType);
        }
    }

    public static RoleType getByName(String name) {
        return roleMap.get(name);
    }

    private int  getValue() {
        return value;
    }

}
