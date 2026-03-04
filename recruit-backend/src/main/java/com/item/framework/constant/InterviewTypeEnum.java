package com.item.framework.constant;

import lombok.Getter;

/**
 * Interview type enum
 */
@Getter
public enum InterviewTypeEnum {
    VIDEO(0, "video"),
    AUDIO(1, "audio"),
    AI_PHONE(2, "phone"),
    ;

    private final int code;

    private final String name;

    InterviewTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Get enum by code
     * @param code interview type code
     * @return InterviewTypeEnum, default VIDEO if code is null or not found
     */
    public static InterviewTypeEnum getByCode(Integer code) {
        if (code == null) {
            return VIDEO;
        }
        for (InterviewTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return VIDEO;
    }

    /**
     * Get enum by name
     * @param name interview type name
     * @return InterviewTypeEnum, default VIDEO if name is null or not found
     */
    public static InterviewTypeEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return VIDEO;
        }
        for (InterviewTypeEnum type : values()) {
            if (type.name.equals(name.trim())) {
                return type;
            }
        }
        return VIDEO;
    }

    /**
     * Check if the name is valid
     * @param name interview type name
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        for (InterviewTypeEnum type : values()) {
            if (type.name.equals(name.trim())) {
                return true;
            }
        }
        return false;
    }

}

