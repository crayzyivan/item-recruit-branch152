package com.item.dto.iam;

import lombok.Data;

import java.util.List;

/**
 * IAM 角色 DTO
 * 对应 RoleDto
 *
 * @author system
 * @version 1.0
 * @since 2025-01-28
 */
@Data
public class IamRoleDTO {
    
    /**
     * 角色ID
     */
    private String id;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 是否默认角色
     */
    private Boolean isDefault;
    
    /**
     * 账户ID
     */
    private String accountId;
    
    /**
     * 角色类型
     * SYSTEM_NORMAL: 系统普通角色
     * SYSTEM_REGISTER: 系统注册角色
     * CUSTOM: 自定义角色
     */
    private String roleType;
    
    /**
     * 该角色的所有权限集合
     */
    private List<IamPermissionEntityDTO> permissions;
}
