package com.item.dto.iam;

import lombok.Data;

import java.util.List;

@Data
public class IamAdminDetailDTO {

    private String id;
    private String accountId;
    private String companyCode;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String userStatus;
    private int userType;
    private boolean primaryUser;
    private String origin;
    private String createdAt;
    private String updatedAt;
    private List<Role> roles;
    private List<Company> companies;

    @Data
    public static class Role {
        private String id;
        private String name;
        private String description;
        private boolean isDefault;
        private String accountId;
        private String roleType;
        private Object permissions;
    }

    @Data
    public static class Company {
        private String companyName;
        private String companyCode;
        private boolean isCurrentTenant;
    }
}
