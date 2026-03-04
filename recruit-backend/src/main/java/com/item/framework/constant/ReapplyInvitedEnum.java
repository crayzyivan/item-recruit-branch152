package com.item.framework.constant;

/**
 * 邀请重新申请状态枚举
 *
 * @author liyunlong
 * @since 2025-09-08
 */
public enum ReapplyInvitedEnum {
    
    /**
     * 未邀请
     */
    NOT_INVITED(0, "not_invited"),
    
    /**
     * 已邀请
     */
    INVITED(1, "invited");

    private final Integer code;
    private final String name;

    ReapplyInvitedEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，如果未找到返回null
     */
    public static ReapplyInvitedEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReapplyInvitedEnum reapplyInvitedEnum : ReapplyInvitedEnum.values()) {
            if (reapplyInvitedEnum.getCode().equals(code)) {
                return reapplyInvitedEnum;
            }
        }
        return null;
    }

    /**
     * 根据name获取枚举
     *
     * @param name 状态名称
     * @return 对应的枚举值，如果未找到返回null
     */
    public static ReapplyInvitedEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ReapplyInvitedEnum reapplyInvitedEnum : ReapplyInvitedEnum.values()) {
            if (reapplyInvitedEnum.getName().equals(name)) {
                return reapplyInvitedEnum;
            }
        }
        return null;
    }

    /**
     * 判断是否已邀请
     *
     * @param code 状态码
     * @return true表示已邀请，false表示未邀请
     */
    public static boolean isInvited(Integer code) {
        return INVITED.getCode().equals(code);
    }

    /**
     * 判断是否未邀请
     *
     * @param code 状态码
     * @return true表示未邀请，false表示已邀请
     */
    public static boolean isNotInvited(Integer code) {
        return NOT_INVITED.getCode().equals(code);
    }
}
