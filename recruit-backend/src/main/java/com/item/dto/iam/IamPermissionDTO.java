package com.item.dto.iam;

import lombok.Data;

import java.util.Map;

/**
 * IAM 权限 DTO
 * 对应 PermissionDTO
 *
 * @author system
 * @version 1.0
 * @since 2025-01-28
 */
@Data
public class IamPermissionDTO {
    
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 权限标题
     */
    private String title;
    
    /**
     * 应用代码
     */
    private String app;
    
    /**
     * 外部信息
     */
    private Map<String, Object> externalInfo;
}
