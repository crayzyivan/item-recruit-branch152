package com.item.dto.iam;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * IAM Primary用户详细信息 DTO
 * 对应 UserDto
 *
 * @author system
 * @version 1.0
 * @since 2025-01-28
 */
@Data
public class IamUserPrimaryDTO {
    
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

    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String userStatus;
    private Boolean primaryUser;
}
