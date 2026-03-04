package com.item.dto.iam;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class IamUserRoleDTO {
    /**
     * Role ID
     * Example: string
     */
    private String id;

    /**
     * Role name
     * Example: string
     */
    private String name;

    /**
     * Role description
     * Example: string
     */
    private String description;

    /**
     * Whether role is default
     * Example: true
     */
    private Boolean isDefault;

    /**
     * Account ID
     * Example: string
     */
    private String accountId;

    /**
     * Role type
     * Example: SYSTEM_NORMAL
     */
    private String roleType;
}
