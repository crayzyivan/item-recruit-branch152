package com.item.vo;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class UserTenantInfoDTO {
    /**
     * 租户code
     */
    private String tenantId;
    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 当前租户 true是； false 否
     */
    private boolean isCurrentTenant;
}
