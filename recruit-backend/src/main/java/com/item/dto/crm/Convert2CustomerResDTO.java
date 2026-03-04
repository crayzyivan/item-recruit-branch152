package com.item.dto.crm;

import lombok.Data;

/**
 * DTO representing CRM convert to customer response information
 * Convert customer response
 *
 * @author hua.liu
 */
@Data
public class Convert2CustomerResDTO {
    /**
     * Customer ID
     * Example: 0
     */
    private Long customerId;
    
    /**
     * Customer code
     * Example: string
     */
    private String customerCode;
    
    /**
     * Error message
     * Example: string
     */
    private String errorMessage;
}