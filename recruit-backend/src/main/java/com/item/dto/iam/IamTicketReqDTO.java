package com.item.dto.iam;

import lombok.Data;

/**
 * IAM ticket request DTO for cross-system session sync
 * Used to request a ticket from IAM system for maintaining login state across systems
 * 
 * @author : lh
 */
@Data
public class IamTicketReqDTO {
    /**
     * User ID for ticket issuance
     */
    private String userId;
    
    /**
     * Client ID identifying the requesting system
     */
    private String clientId;
}
