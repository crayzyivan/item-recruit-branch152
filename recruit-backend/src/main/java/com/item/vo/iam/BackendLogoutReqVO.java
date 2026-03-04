package com.item.vo.iam;

import lombok.Data;

/**
 * Backend logout request VO for cross-system session sync
 * Used in Controller layer for API input validation
 *
 * @author : lh
 */
@Data
public class BackendLogoutReqVO {
    /**
     * User ID for logout operation
     */
    private String userId;
}
