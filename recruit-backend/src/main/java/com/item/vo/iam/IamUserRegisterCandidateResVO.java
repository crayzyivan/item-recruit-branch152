package com.item.vo.iam;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户注册响应DTO
 * @author : lh
 */
@Data
public class IamUserRegisterCandidateResVO {
    
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 账户ID
     */
//    private String accountId;
    
    /**
     * 公司代码
     */
//    private String companyCode;
    
    /**
     * 公司名称
     */
//    private String companyName;
    
    /**
     * 联系电话
     */
    private String contactNumber;
    
    /**
     * 邮箱地址
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
//    private String userType;
    
    /**
     * 用户类型名称
     */
//    private String userTypeName;
    
    /**
     * 是否为主用户
     */
//    private Boolean primaryUser;
    
    /**
     * 用户角色列表
     */
//    private List<IamUserRoleDTO> userRoles;
    
    /**
     * 用户角色ID列表
     */
//    private List<String> userRoleIds;
    
    /**
     * 用户权限列表
     */
//    private List<IamUserPermissionDTO> userPermissions;
    
    /**
     * 用户标签
     */
    private List<String> userTags;
    
    /**
     * CRM用户ID
     */
//    private String crmUserId;
    
    /**
     * TMS用户ID
     */
//    private String tmsUserId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 扩展信息
     */
//    private Map<String, Object> externalInfo;
    
    /**
     * 来源
     */
//    private String origin;
    
    /**
     * 授权的应用代码列表
     */
//    private List<String> grantedAppCodes;
}