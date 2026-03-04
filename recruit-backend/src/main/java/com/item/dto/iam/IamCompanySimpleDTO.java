package com.item.dto.iam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * IAM公司简单信息DTO
 * 用于表示用户关联的公司基本信息
 *
 * @author lh
 * @version 1.0
 */
@Data
public class IamCompanySimpleDTO {
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 公司代码
     * 唯一标识公司的代码
     */
    private String companyCode;

    /**
     * 是否上次选择租户
     */
    @JsonProperty("isCurrentTenant")
    private Boolean isCurrentTenant;
}
