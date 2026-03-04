package com.item.dto.iam;

import lombok.Data;

/**
 * DTO representing IAM sign up manager and company response information
 * /v1/register/company response
 * https://apifox.com/apidoc/shared/a35148ba-4cde-43e2-a450-7b245bf8462d/api-287034056
 *
 * @author hua.liu
 */
@Data
public class IamSignUpManagerCompanyResDTO {
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
} 