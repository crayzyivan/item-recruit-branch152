package com.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class UserTenantSwitchReqDTO {

    /**
     * 切换的目标租户
     */
    @NotBlank(message = "switchTargetTenantId must not be blank")
    private String switchTargetTenantId;
}
