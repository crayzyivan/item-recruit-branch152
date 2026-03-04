package com.item.vo;

import lombok.Data;

/**
 * 根据申请ID返回的用户信息
 * 包含租户ID、用户ID和用户姓名
 *
 * @since 2025-11-10
 */
@Data
public class ApplicationUserInfoVO {
    
    /**
     * 租户ID（公司代码）
     */
    private String tenantId;
    
    /**
     * 用户ID（IAM用户ID）
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
}

