package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * lead customer company 映射关系实体类
 * </p>
 *
 * @author lh on 2025/7/29
 * @since 1.0.0
 */
@Data
@TableName(value = "r_leads_customer_company")
public class LeadsCustomerCompanyEntity implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "crm_leads_id")
    private Long crmLeadsId;
    
    @TableField(value = "crm_customer_id")
    private Long crmCustomerId;
    
    @TableField(value = "crm_customer_code")
    private String crmCustomerCode;
    
    @TableField(value = "crm_leads_customer_status")
    private Integer crmLeadsCustomerStatus;
    
    @TableField(value = "central_company_code")
    private String centralCompanyCode;
    
    @TableField(value = "central_lead_company_id")
    private Long centralLeadCompanyId;
    
    @TableField(value = "central_manager_id")
    private Long centralManagerId;

    @TableField(value = "central_company_id")
    private Long centralCompanyId;
    
    @TableField(value = "response_msg")
    private String responseMsg;

    @TableField(value = "origin")
    private Integer origin;
    
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}