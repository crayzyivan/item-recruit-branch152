package com.item.dto.iam;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Creates a new sub user within the current company
 * /v1/create/user
 * https://apifox.com/apidoc/shared/a35148ba-4cde-43e2-a450-7b245bf8462d/api-287034057
 *
 * @author hua.liu
 */
@Data
public class IamCreateSubUserDTO {
    /**
     * required
     * Company code
     * Example: unin0001
     */
    private String companyCode;

    /**
     * required
     * User name
     * Example: johndoe
     */
    private String userName;

    /**
     * Raw password for the user
     * Example: password123
     */
    private String rawPassword;

    /**
     * required
     * User email address
     * Example: john.doe@example.com
     */
    private String email;

    /**
     * User first name
     * Example: John
     */
    private String firstName;

    /**
     * User last name
     * Example: Doe
     */
    private String lastName;

    /**
     * User contact number
     * Example: +86-13800138000
     */
    private String contactNumber;

    /**
     * 0:Internal User、1:External User
     * Allowed values: 0 1
     * Example: 1
     */
    private String userType;

    /**
     * user Tags
     * Employee,Admin,Supervisor,Dispatcher,Employee Driver
     * Example: Employee
     */
    private List<String> userTags;

    /**
     * Origin of the user
     * Example: SYSTEM
     */
    private String origin;

    /**
     * External information
     * Example: {}
     */
    private Map<String, Object> externalInfo;

    /**
     * Event code for points awarding during account creation
     * Example: REGISTER/CREATE_SUB
     */
    private String eventCode;

    /**
     * App code for the application
     * Example: APPCODE
     */
    private String appcode;
} 