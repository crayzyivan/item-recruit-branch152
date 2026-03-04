package com.item.pgentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * PostgreSQL工作历史实体
 * 对应candidates.employment_history表
 * 
 * UUID字段处理说明：
 * - candidateId字段使用String类型存储UUID
 * - 使用UuidUtils工具类进行String与UUID之间的转换
 * - 确保与PostgreSQL数据库的UUID类型兼容
 * 
 * @author system
 */
@Data
@TableName("candidates.employment_history")
public class PgEmploymentHistory {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("candidate_id")
    private String candidateId;
    
    @TableField("company_name")
    private String companyName;
    
    @TableField("job_title")
    private String jobTitle;
    
    @TableField("start_date")
    private LocalDate startDate;
    
    @TableField("end_date")
    private LocalDate endDate;
    
    @TableField("current_employer")
    private Boolean currentEmployer;
    
    @TableField("responsibilities")
    private String responsibilities;
    
    @TableField("created_on")
    private OffsetDateTime createdOn;
    
    @TableField("updated_on")
    private OffsetDateTime updatedOn;
}
