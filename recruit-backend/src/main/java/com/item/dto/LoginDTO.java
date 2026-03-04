package com.item.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String token;
    private String userName;
    private String email;
    private String companyName;
    private String loginRole;
    private String password;
}