package com.item.dto.iam;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * @author : lh
 */
@Data
public class ExistsUserDTO {
    /**
     * 必须
     */
    @NotNull(message = "companyCode must not be null")
    @NotBlank(message = "companyCode must not be blank")
    private String companyCode;
    /**
     * 必须
     */
    @NotNull(message = "userName must not be null")
    @NotBlank(message = "userName must not be blank")
    private String userName;
    private String rawPassword;
    /**
     * 必须
     */
    @NotNull(message = "email must not be null")
    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a well-formed email address")
    private String email;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String userType;
    private String userTags;
    private String origin;
    private Map<String, Object> externalInfo;
}
