package com.item.dto.iam;

import lombok.Data;

import java.util.Map;

/**
 * IAM 权限实体 DTO
 * 对应 PermissionEntity
 *
 * @author system
 * @version 1.0
 * @since 2025-01-28
 */
@Data
public class IamPermissionEntityDTO {
    
    /**
     * 创建时间
     */
    private String createdAt;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新时间
     */
    private String updatedAt;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 权限标题
     */
    private String title;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 应用代码
     */
    private String app;
    
    /**
     * 权限组
     */
    private String group;
    
    /**
     * 是否删除
     */
    private Boolean deleted;
    
    /**
     * 外部信息
     */
    private Map<String, Object> externalInfo;
}
