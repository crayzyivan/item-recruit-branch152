package com.item.vo.iam;

import lombok.Data;

/**
 * IAM ticket response VO for cross-system session sync
 * Used in Controller layer for API output
 * 
 * @author : lh
 */
@Data
public class IamTicketResVO {
    /**
     * The issued ticket for cross-system authentication
     */
    private String ticket;
}
