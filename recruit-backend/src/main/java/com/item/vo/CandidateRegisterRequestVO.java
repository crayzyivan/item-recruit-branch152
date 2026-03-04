package com.item.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CandidateRegisterRequestVO {
    @NotBlank(message = "The user name cannot be empty.")
    private String name;
    private String firstName;
    private String lastName;
    @NotBlank(message = "The email cannot be empty")
    @Email(message = "The email format is incorrect")
    private String email;
    @NotBlank(message = "The password cannot be empty.")
    private String password;
    @NotBlank(message = "The phone number cannot be empty.")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,4}[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,15}$",message = "The mobile phone number format is incorrect.")
    private String phoneNumber;
} 