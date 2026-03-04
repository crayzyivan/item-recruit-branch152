package com.item.dto.iam;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * IAM创建用户请求DTO
 * 对应OpenAPI /openapi/v1/users 接口
 * 
 * @author YaRong.Guo
 */
@Data
public class IamCreateUserReqDTO {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 原始密码
     */
    private String rawPassword;

    /**
     * 公司编码
     */
    private String companyCode;

    /**
     * 名字
     */
    private String firstName;

    /**
     * 姓氏
     */
    private String lastName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 联系电话
     */
    private String contactNumber;

    /**
     * 用户类型
     * 0: 普通用户
     * 1: 管理员用户
     */
    private Integer userType;

    /**
     * 用户标签
     */
    private List<String> userTags;

    /**
     * 来源
     * 例如: "marketplace,recruit"
     */
    private String origin;

    /**
     * 外部信息
     */
    private Map<String, Object> externalInfo;

    /**
     * 授权的应用编码列表
     */
    private List<String> grantedAppCodes;
}
