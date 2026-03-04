package com.item.dto;

import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import lombok.Data;

/**
 * Personal user registration request DTO
 * Contains user information for C-end user registration
 * 
 * @author lh
 */
@Data
public class PersonalUserRegisterReqDTO {
    
    /**
     * User name for login Required
     */
    private String userName;
    
    /**
     * Raw password (will be encrypted by IAM)  Required
     */
    private String rawPassword;
    
    /**
     * First name of the user Required
     */
    private String firstName;
    
    /**
     * Last name of the user Required
     */
    private String lastName;
    
    /**
     * Email address (used for notification and login) Required
     */
    private String email;
    
    /**
     * Contact phone number
     */
    private String contactNumber;
    
    /**
     * Validate required fields
     * Throws BusinessException if any required field is null or empty
     * 
     * @throws BusinessException when required field is missing
     */
    public void validate() throws BusinessException {
        if (userName == null || userName.trim().isEmpty()) {
            throw BusinessException.of(GlobalStatusCode.PARAM_MISSING, "userName is required");
        }
        
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw BusinessException.of(GlobalStatusCode.PARAM_MISSING, "rawPassword is required");
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            throw BusinessException.of(GlobalStatusCode.PARAM_MISSING, "firstName is required");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            throw BusinessException.of(GlobalStatusCode.PARAM_MISSING, "lastName is required");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw BusinessException.of(GlobalStatusCode.PARAM_MISSING, "email is required");
        }
        
        // contactNumber is optional, no validation needed
    }

}

