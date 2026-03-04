package com.item.dto.iam;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * IAM创建用户响应DTO
 * 对应OpenAPI /openapi/v1/users 接口响应
 * 
 * @author YaRong.Guo
 */
@Data
public class IamCreateUserResDTO {

    /**
     * 用户ID
     */
    private String id;

    /**
     * 账户ID
     */
    private String accountId;

    /**
     * 公司编码
     */
    private String companyCode;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 联系电话
     */
    private String contactNumber;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 名字
     */
    private String firstName;

    /**
     * 姓氏
     */
    private String lastName;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户状态
     */
    private String userStatus;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 用户类型名称
     */
    private String userTypeName;

    /**
     * 是否为主用户
     */
    private Boolean primaryUser;

    /**
     * 用户角色列表
     */
    private List<UserRole> userRoles;

    /**
     * 用户角色ID列表
     */
    private List<String> userRoleIds;

    /**
     * 用户权限列表
     */
    private List<UserPermission> userPermissions;

    /**
     * 用户标签
     */
    private List<String> userTags;

    /**
     * CRM用户ID
     */
    private String crmUserId;

    /**
     * TMS用户ID
     */
    private String tmsUserId;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;

    /**
     * 外部信息
     */
    private Map<String, Object> externalInfo;

    /**
     * 来源
     */
    private String origin;

    /**
     * 授权的应用编码列表
     */
    private List<String> grantedAppCodes;

    /**
     * 账户生效日期
     */
    private String accountEffectiveDate;

    /**
     * 用户角色信息
     */
    @Data
    public static class UserRole {
        private String id;
        private String name;
        private String description;
        private Boolean isDefault;
        private String accountId;
        private String roleType;
        private List<UserPermission> permissions;
    }

    /**
     * 用户权限信息
     */
    @Data
    public static class UserPermission {
        private String createdAt;
        private String createdBy;
        private String updatedAt;
        private String updatedBy;
        private Integer id;
        private String name;
        private String title;
        private Integer parentId;
        private String app;
        private String group;
        private Boolean deleted;
        private Map<String, Object> externalInfo;
    }
}
