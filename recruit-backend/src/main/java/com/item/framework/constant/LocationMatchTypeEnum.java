package com.item.framework.constant;

import lombok.Getter;

/**
 * 地理位置匹配类型枚举
 * 用于标识模糊搜索结果的匹配类型
 * 
 * @author yarong.guo
 * @version 1.0
 * @since 2025-01-27
 */
@Getter
public enum LocationMatchTypeEnum {
    
    /**
     * 城市匹配
     */
    CITY("city", "城市"),
    
    /**
     * 省份/州匹配
     */
    STATE("state", "省份"),
    
    /**
     * 国家匹配
     */
    COUNTRY("country", "国家");
    
    private final String code;
    private final String name;
    
    LocationMatchTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return LocationMatchTypeEnum，如果未找到返回null
     */
    public static LocationMatchTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (LocationMatchTypeEnum type : values()) {
            if (type.code.equals(code.trim())) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 检查代码是否有效
     * @param code 代码
     * @return true如果有效，false否则
     */
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
}
