package com.item.vo;

import lombok.Data;

@Data
public class LoginResponseVO {
    private String token;
    private Long userid;
    private String userName;
    private String email;
    private String companyName;
    private String loginRole;
    private String phoneNumber;
}