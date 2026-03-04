package com.item.dto.iam;

import lombok.Data;

/**
 * 用户注册请求DTO
 * @author : lh
 */
@Data
public class IamUserRegisterReqDTO {
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 公司代码
     */
    private String companyCode;
    
    /**
     * 原始密码
     */
    private String rawPassword;
    
    /**
     * 来源
     */
    private String source;
    
    /**
     * 名字
     */
    private String firstName;
    
    /**
     * 姓氏
     */
    private String lastName;
    
    /**
     * 员工代码
     */
    private String employeeCode;
    
    /**
     * 申请备注
     */
    private String applicationNote;
    
    /**
     * 员工验证
     */
    private Boolean employeeVerify;
}