package com.item.vo.iam;

import lombok.Data;

/**
 * Backend logout response VO for cross-system session sync
 * Used in Controller layer for API output
 *
 * @author : lh
 */
@Data
public class BackendLogoutResVO {
    /**
     * The logout ticket for cross-system authentication
     */
    private String userId;
}
