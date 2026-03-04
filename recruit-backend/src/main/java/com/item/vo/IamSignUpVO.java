package com.item.vo;

import lombok.Data;

/**
 * DTO representing IAM sign up manager and company response information
 * adapter
 * @author hua.liu
 */
@Data
public class IamSignUpVO {
    /**
     * Company ID
     * Example: 18385306976081
     */
    private Long companyId;
    
    /**
     * Company code
     * Example: unin0001
     */
    private String companyCode;
    
    /**
     * Company name
     * Example: unins
     */
    private String companyName;
    
    /**
     * Manager ID
     * Example: 18385306976081984
     */
    private Long managerId;
    
    /**
     * Manager user email
     * Example: john.doe@example.com
     */
    private String managerUserEmail;
    
    /**
     * Manager user name
     * Example: john
     */
    private String managerUserName;
    
    /**
     * Lead company ID
     * Example: string
     */
    private String leadCompanyId;

    private String customerCode;
} 