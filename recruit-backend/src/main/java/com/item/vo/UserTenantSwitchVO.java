package com.item.vo;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class UserTenantSwitchVO {
//    /**
//     * 当前登录人当前租户
//     */
//    private String currentTenantId;

    /**
     * 切换的目标租户
     */
    private String switchTargetTenantId;

    /**
     * 访问token
     */
    private String accessToken;

    /**
     * 刷新token
     */
    private String refreshToken;
}
