package com.item.dto.iam;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册请求DTO
 * @author : lh
 */
@Data
public class IamUserRegisterCandidateReqDTO {
    
    /**
     * 用户名
     */
    @NotBlank(message = "userName must not be blank")
    private String userName;
    
    /**
     * 邮箱地址
     */
    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a well-formed email address")
    private String email;
    
    /**
     * 公司代码
     */
//    private String companyCode;
    
    /**
     * 原始密码
     */
    private String rawPassword;
    
    /**
     * 来源
     */
//    private String source;
    
    /**
     * 名字
     */
    @NotBlank(message = "firstName must not be blank")
    private String firstName;
    
    /**
     * 姓氏
     */
    @NotBlank(message = "lastName must not be blank")
    private String lastName;

    private String middleName;
    
    /**
     * 员工代码
     */
//    private String employeeCode;
    
    /**
     * 申请备注
     */
//    private String applicationNote;
    
    /**
     * 员工验证
     */
//    private Boolean employeeVerify;
}