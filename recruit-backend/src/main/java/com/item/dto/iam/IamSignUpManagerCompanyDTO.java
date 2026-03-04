package com.item.dto.iam;

import lombok.Data;

/**
 * DTO representing IAM sign up manager and company information
 * /v1/register/company
 * https://apifox.com/apidoc/shared/a35148ba-4cde-43e2-a450-7b245bf8462d/api-287034056
 *
 * @author hua.liu
 */
@Data
public class IamSignUpManagerCompanyDTO {
    /**
     * required
     * managerUserEmail
     * Example: john.doe@example.com
     */
    private String managerUserEmail;
    /**
     * required
     * managerUserName
     * Example: john
     */
    private String managerUserName;

    /**
     * required
     * the managerUser password
     * Example:  password123
     */
    private String password;
    /**
     * tax identification number
     * Example: 10000303000
     */
    private String tax;
    /**
     * the managerUser contact number
     * Example:  +86-13800138000
     */
    private String contactNumber;
    /**
     * required
     * companyName
     * Example: unins company
     */
    private String companyName;
    /**
     * companyaAlias
     * Example:  unins
     */
    private String companyaAlias;
    /**
     * companyNumber
     * Example: 1861610912917319681
     */
    private String companyNumber;
    /**
     * 0:PENDING、1:ACTIVE、2:INACTIVE、3:FROZEN
     * Example: 1
     */
    private Integer companyStatus;
    /**
     * 0:PRIVATE_COMPANY、1:PUBLIC_COMPANY、2:SUBSIDIARY、3:HOLDING_COMPANY、4:JOINT_VENTURE、5:PARTNERSHIP、6:LIMITED_LIABILITY_COMPANY、7:CORPORATION、8:SOLE_PROPRIETORSHIP、9:NONPROFIT_ORGANIZATION、10:OTHER
     * Example: 1
     */
    private String companyType;
    /**
     * description
     * Example: this is company description
     */
    private String description;
    /**
     * 0:Unis、1:Item、2:Other
     * Example:  1
     */
    private Integer companySource;
    /**
     * businessCategory
     * Example: Accounting and Financial Services
     */
    private String businessCategory;
    /**
     * required
     * address1
     */
    private String address1;
    /**
     *
     */
    private String address2;
    /**
     * required
     * city
     */
    private String city;
    /**
     * required
     * state
     */
    private String state;
    /**
     *
     */
    private String zipcode;
    /**
     * county
     */
    private String county;
    /**
     * required
     * country
     */
    private String country;
    /**
     * firstName of company contact person
     */
    private String firstName;
    /**
     * lastName of company contact person
     */
    private String lastName;
    /**
     * telephone of company contact person
     */
    private String telephone;
    /**
     * email of company contact person
     */
    private String email;
    /**
     * fax of company contact person
     */
    private String fax;
    /**
     *
     */
    private String notes;
    /**
     *
     */
    private Long parentId;
    /**
     *
     */
    private String website;
    /**
     *
     */
    private String federalId;
    /**
     *
     */
    private String logopath;
    /**
     * Awards points during account registration. No points awarded if not provided.
     * Example: REGISTER/CREATE_SUB
     */
    private String eventCode;
    /**
     * App code for the application. No app code if not provided.
     * Example: APPCODE
     */
    private String appcode;
} 