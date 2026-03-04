package com.item.dto.iam;

import lombok.Data;

/**
 * IAM应用信息DTO
 * 表示用户可访问的应用系统信息
 *
 * @author lh
 * @version 1.0
 */
@Data
public class IamAppDTO {
    
    /**
     * 应用代码
     * 唯一标识应用的代码
     */
    private String code;
    
    /**
     * 应用名称
     */
    private String name;
    
    /**
     * 应用描述
     */
    private String description;
    
    /**
     * 回调URL
     * 用于OAuth认证或其他回调场景
     */
    private String callbackUrl;
}
