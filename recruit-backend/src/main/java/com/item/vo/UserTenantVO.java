package com.item.vo;

import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class UserTenantVO {
    /**
     * 当前登录人的租户列表
     */
    private List<UserTenantInfoDTO> tenants;
    /**
     * 当前登录选择的租户
     */
    private UserTenantInfoDTO currentTenant;
}
