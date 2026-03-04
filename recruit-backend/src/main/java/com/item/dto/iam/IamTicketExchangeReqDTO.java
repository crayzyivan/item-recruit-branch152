package com.item.dto.iam;

import lombok.Data;

/**
 * IAM ticket exchange request DTO for cross-system session sync
 * Used to exchange a ticket for access tokens from IAM system
 * 
 * @author : lh
 */
@Data
public class IamTicketExchangeReqDTO {
    /**
     * The ticket to be exchanged for access tokens
     */
    private String ticket;
}
