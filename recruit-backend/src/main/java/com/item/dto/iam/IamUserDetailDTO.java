package com.item.dto.iam;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * IAM 用户详细信息 DTO
 * 对应 UserDto
 *
 * @author system
 * @version 1.0
 * @since 2025-01-28
 */
@Data
public class IamUserDetailDTO {
    
    /**
     * 用户唯一标识符
     */
    private String id;
    
    /**
     * 用户所属账户ID
     */
    private String accountId;
    
    /**
     * 公司代码
     */
    private String companyCode;
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 用户联系电话
     */
    private String contactNumber;
    
    /**
     * 用户邮箱地址
     */
    private String email;
    
    /**
     * 用户名字
     */
    private String firstName;
    
    /**
     * 用户姓氏
     */
    private String lastName;
    
    /**
     * 登录用户名
     */
    private String userName;
    
    /**
     * 用户账户状态
     */
    private String userStatus;
    
    /**
     * 用户类型枚举
     * INTERNAL: 内部用户
     * EXTERNAL: 外部用户
     * THIRD: 第三方用户
     * PERSONAL: 个人用户
     */
    private String userType;
    
    /**
     * 用户类型显示名称
     */
    private String userTypeName;
    
    /**
     * 是否为账户主用户
     */
    private Boolean primaryUser;
    
    /**
     * 分配给用户的角色列表
     */
    private List<IamRoleDTO> userRoles;
    
    /**
     * 分配给用户的角色ID集合
     */
    private List<String> userRoleIds;
    
    /**
     * 授予用户的权限列表
     */
    private List<IamPermissionDTO> userPermissions;
    
    /**
     * 与用户关联的标签集合
     */
    private List<String> userTags;
    
    /**
     * CRM系统用户标识符
     */
    private String crmUserId;
    
    /**
     * TMS系统用户标识符
     */
    private String tmsUserId;
    
    /**
     * 用户创建时间戳
     */
    private String createdAt;
    
    /**
     * 用户最后更新时间戳
     */
    private String updatedAt;
    
    /**
     * 外部系统集成信息
     */
    private Map<String, Object> externalInfo;
    
    /**
     * 用户数据来源
     */
    private String origin;
    
    /**
     * 用户有权限访问的应用代码集合
     */
    private List<String> grantedAppCodes;
    
    /**
     * 账户生效日期
     */
    private String accountEffectiveDate;
}
