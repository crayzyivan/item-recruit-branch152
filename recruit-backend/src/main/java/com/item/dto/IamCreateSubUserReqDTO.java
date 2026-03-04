package com.item.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Creates a new sub user within the current company
 *
 * @author hua.liu
 */
@Data
public class IamCreateSubUserReqDTO {

    /**
     * required
     * User name
     * Example: johndoe
     */
    @NotNull(message = "userName must not be null")
    @NotBlank(message = "userName must not be blank")
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
    @NotNull(message = "email must not be null")
    @Email(message = "email must be a well-formed email address")
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
     * User type
     * Example: 0
     */
    private Integer userType;

    /**
     * User tags
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
} 