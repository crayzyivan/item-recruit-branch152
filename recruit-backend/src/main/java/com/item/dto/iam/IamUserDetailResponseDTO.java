package com.item.dto.iam;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * IAM用户详细信息响应DTO
 * 用于接收IAM系统返回的完整用户信息
 *
 * @author lh
 * @version 1.0
 */
@Data
public class IamUserDetailResponseDTO {
    
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 账户ID
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
     * 用户名
     */
    private String userName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 名
     */
    private String firstName;
    
    /**
     * 姓
     */
    private String lastName;
    
    /**
     * 联系电话
     */
    private String contactNumber;
    
    /**
     * 用户状态
     * ACTIVE: 激活
     * INACTIVE: 未激活
     * SUSPENDED: 暂停
     */
    private String userStatus;
    
    /**
     * 用户类型
     * INTERNAL: 内部用户
     * EXTERNAL: 外部用户
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
     * 用户标签列表
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
     * 用户来源
     * LDAP: LDAP系统
     * MANUAL: 手动创建
     * SSO: 单点登录
     */
    private String origin;
    
    /**
     * 员工ID
     */
    private String employeeId;
    
    /**
     * 外部扩展信息
     * 存储额外的用户信息，如LDAP DN等
     */
    private Map<String, Object> externalInfo;
    
    /**
     * 账户生效日期
     */
    private String accountEffectiveDate;
    
    /**
     * 创建时间
     */
    private String createdAt;
    
    /**
     * 更新时间
     */
    private String updatedAt;
    
    /**
     * 用户可访问的应用列表
     */
    private List<IamAppDTO> apps;
    
    /**
     * 授权的应用代码列表
     */
    private List<String> grantedAppCodes;
    
    /**
     * 用户角色列表
     */
    private List<IamRoleDTO> roles;
    
    /**
     * 用户关联的公司代码列表
     */
    private List<String> companyCodes;
    
    /**
     * 用户关联的公司列表
     */
    private List<IamCompanySimpleDTO> companies;
}
