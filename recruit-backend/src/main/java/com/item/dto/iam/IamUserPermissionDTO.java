package com.item.dto.iam;

import lombok.Data;

import java.util.Map;

/**
 * @author : lh
 */
@Data
public class IamUserPermissionDTO {
    /**
     * Permission ID
     * Example: 0
     */
    private Long id;

    /**
     * Permission name
     * Example: string
     */
    private String name;

    /**
     * Parent permission ID
     * Example: 0
     */
    private Long parentId;

    /**
     * Permission title
     * Example: string
     */
    private String title;

    /**
     * Application name
     * Example: string
     */
    private String app;

    /**
     * External information
     */
    private Map<String, Object> externalInfo;
}
