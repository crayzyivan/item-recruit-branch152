package com.item.dto.iam;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <a href="https://apifox.com/apidoc/shared/a35148ba-4cde-43e2-a450-7b245bf8462d/api-287034058">接口文档</a>
 * DTO representing IAM user information
 *
 * @author hua.liu
 */
@Data
public class IamUserDTO {
    private String id;
    private String accountId;
    private String companyCode;
    private String contactNumber;
    private String email;
    private String firstName;
    private String lastName;
    private String userName;
    private String userStatus;
    private Integer userType;
    private Boolean primaryUser;
    private List<IamUserRoleDTO> userRoles;
    private LocalDateTime createdAt;
    private String origin;
} 