package com.item.pgentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * PostgreSQL申请表实体
 * 对应candidates.applications表
 * 
 * UUID字段处理说明：
 * - id、candidateId、createdBy、updatedBy字段使用String类型存储UUID
 * - 主键使用@TableId(type = IdType.INPUT)配置
 * - 使用UuidUtils工具类进行String与UUID之间的转换
 * - 确保与PostgreSQL数据库的UUID类型兼容
 * 
 * @author system
 */
@Data
@TableName("candidates.applications")
public class PgApplication {
    
    @TableId(type = IdType.INPUT)
    private String id;
    
    @TableField("candidate_id")
    private String candidateId;
    
    @TableField("slug")
    private String slug;
    
    @TableField("job_id")
    private Integer jobId;
    
    @TableField("status")
    private String status;
    
    @TableField("currency_id")
    private Integer currencyId;
    
    @TableField("expected_salary")
    private BigDecimal expectedSalary;
    
    @TableField("\"salaryType\"")
    private String salaryType;
    
    @TableField("available_from")
    private OffsetDateTime availableFrom;
    
    @TableField("created_on")
    private OffsetDateTime createdOn;
    
    @TableField("updated_on")
    private OffsetDateTime updatedOn;
    
    @TableField("created_by")
    private String createdBy;
    
    @TableField("updated_by")
    private String updatedBy;
}
