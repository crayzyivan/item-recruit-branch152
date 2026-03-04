package com.item.vo;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class LoginRequestVO {
    @Email(message = "The email format is incorrect")
    private String email;
    private String password;


} 