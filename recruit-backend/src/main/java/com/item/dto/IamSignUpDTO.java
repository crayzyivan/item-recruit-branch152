package com.item.dto;

import com.item.framework.annotation.AllowedInteger;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO representing IAM sign up manager and company information
 * adapter
 *
 * @author hua.liu
 */
@Data
public class IamSignUpDTO {
    /**
     * required
     * managerUserEmail
     * Example: john.doe@example.com
     */
    @NotBlank(message = "managerUserEmail must not be blank")
    @Email(message = "managerUserEmail must be a well-formed email address")
    private String managerUserEmail;
    /**
     * required
     * managerUserName
     * Example: john
     */
    @NotBlank(message = "managerUserName must not be blank")
    private String managerUserName;

    /**
     * required
     * the managerUser password
     * Example:  password123
     */
    @NotBlank(message = "password must not be blank")
    private String password;
    /**
     * tax identification number
     * Example: 10000303000
     */
    @Size(max = 40, message = "tax size must be between {min} and {max}")
    private String tax;
    /**
     * the managerUser contact number
     * Example:  +86-13800138000
     */
    @Size(max = 20, message = "contactNumber size must be between {min} and {max}")
    private String contactNumber;
    /**
     * required
     * companyName
     * Example: unins company
     */
    @NotBlank(message = "companyName must not be blank")
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
    @Size(max = 30, message = "companyNumber size must be between {min} and {max}")
    private String companyNumber;
    /**
     * 0:PENDING、1:ACTIVE、2:INACTIVE、3:FROZEN
     * Example: 1
     */
    @Min(value = 0, message = "companySource must be greater than or equal to {value}")
    @Max(value = 3, message = "companySource must be less than or equal to {value}")
    private Integer companyStatus;
    /**
     * 0:PRIVATE_COMPANY、1:PUBLIC_COMPANY、2:SUBSIDIARY、3:HOLDING_COMPANY、4:JOINT_VENTURE、5:PARTNERSHIP、6:LIMITED_LIABILITY_COMPANY、7:CORPORATION、8:SOLE_PROPRIETORSHIP、9:NONPROFIT_ORGANIZATION、10:OTHER
     * Example: 1
     */
    @AllowedInteger(values = {0,1,2,3,4,5,6,7,8,9,10}, message = "companyType must be one of the allowed values: {values}")
    private Integer companyType;
    /**
     * description
     * Example: this is company description
     */
    private String description;
    /**
     * 0:Unis、1:Item、2:Other
     * Example:  1
     */
    @Min(value = 0, message = "companySource must be greater than or equal to {value}")
    @Max(value = 2, message = "companySource must be less than or equal to {value}")
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
    @NotBlank(message = "address1 must not be blank")
    private String address1;
    /**
     *
     */
    private String address2;
    /**
     * required
     * city
     */
    @NotBlank(message = "city must not be blank")
    private String city;
    /**
     * required
     * state
     */
    @NotBlank(message = "state must not be blank")
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
    @NotBlank(message = "country must not be blank")
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
    @Email(message = "email must be a well-formed email address")
    private String email;
    /**
     * fax of company contact person
     */
    @Size(max = 60, message = "fax size must be between {min} and {max}")
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
} 