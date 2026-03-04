package com.item.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * lead customer company 映射关系VO
 * </p>
 *
 * @author lh on 2025/7/29
 * @since 1.0.0
 */
@Data
public class LeadsCustomerCompanyVO implements Serializable {
    
    private Long id;
    
    private Long crmLeadsId;
    
    private Long crmCustomerId;
    
    private String crmCustomerCode;
    
    private Integer crmLeadsCustomerStatus;
    
    private String centralCompanyCode;
    
    private Long centralLeadCompanyId;
    
    private Long centralManagerId;
    
    private String responseMsg;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}