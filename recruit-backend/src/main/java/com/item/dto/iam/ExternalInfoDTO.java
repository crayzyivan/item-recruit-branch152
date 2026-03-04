package com.item.dto.iam;

import lombok.Data;

/**
 * External information DTO for IAM user context
 * Used to store extended user information including employee ID and account type
 * 
 * @author hua.liu
 */
@Data
public class ExternalInfoDTO {
    
    /**
     * Account type - AccountType.PERSONAL indicates C-end personal user
     */
    private String accountType;
    
    /**
     * Recruit account type - used to determine if user is primary recruit account
     * Values: undefined, primary
     */
    private String recruitAccountType;
}
