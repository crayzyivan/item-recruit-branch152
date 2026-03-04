package com.item.dto.iam;

import lombok.Data;

/**
 * IAM ticket response DTO for cross-system session sync
 * Contains the issued ticket for maintaining login state across systems
 * 
 * @author : lh
 */
@Data
public class IamTicketResDTO {
    /**
     * The issued ticket for cross-system authentication
     */
    private String ticket;
}
