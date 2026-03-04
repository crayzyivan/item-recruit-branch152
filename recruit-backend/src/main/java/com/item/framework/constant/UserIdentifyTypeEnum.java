package com.item.framework.constant;

import com.item.framework.error.BusinessException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : lh
 */
@Getter
public enum UserIdentifyTypeEnum {
    CANDIDATE(1, RoleType.CANDIDATE.getIdentity(), RoleType.CANDIDATE.getRole(),"candidate"),
    MASTER_RECRUIT(2, RoleType.MASTER_USER.getIdentity(), RoleType.MASTER_USER.getRole(), "master recruit"),
    SUB_RECRUIT(3, RoleType.SUB_USER.getIdentity(), RoleType.SUB_USER.getRole(), "sub recruit"),
    ;

    private final int code;
    private final String desc;
    private final String identify;
    private final String role;

    UserIdentifyTypeEnum(int code, String identify, String role, String desc) {
        this.code = code;
        this.desc = desc;
        this.identify = identify;
        this.role = role;
    }
    private static final class Holder {
        private static Map<Integer, UserIdentifyTypeEnum> MAP = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(UserIdentifyTypeEnum::getCode, Function.identity()));
    }

    /**
     * 通过 code 获取枚举
     */
    public static UserIdentifyTypeEnum getByCode(Integer code) {
        if (code == null) {
            throw BusinessException.of(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT);
        }
        UserIdentifyTypeEnum userIdentifyTypeEnum = Holder.MAP.getOrDefault(code, null);
        if (userIdentifyTypeEnum == null) {
            throw BusinessException.of(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_FOUNT);
        }
        return userIdentifyTypeEnum;
    }
}
