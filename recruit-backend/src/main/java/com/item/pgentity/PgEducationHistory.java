package com.item.pgentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * PostgreSQL教育历史实体
 * 对应candidates.education_history表
 * 
 * UUID字段处理说明：
 * - candidateId字段使用String类型存储UUID
 * - 使用UuidUtils工具类进行String与UUID之间的转换
 * - 确保与PostgreSQL数据库的UUID类型兼容
 * 
 * @author system
 */
@Data
@TableName("candidates.education_history")
public class PgEducationHistory {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("candidate_id")
    private String candidateId;
    
    @TableField("\"institutionType\"")
    private String institutionType;
    
    @TableField("institution_name")
    private String institutionName;
    
    @TableField("start_date")
    private LocalDate startDate;
    
    @TableField("end_date")
    private LocalDate endDate;
    
    @TableField("graduated")
    private Boolean graduated;
    
    @TableField("\"degreeEarned\"")
    private String degreeEarned;
    
    @TableField("major")
    private String major;
    
    @TableField("minor")
    private String minor;
    
    @TableField("created_on")
    private OffsetDateTime createdOn;
    
    @TableField("updated_on")
    private OffsetDateTime updatedOn;
}
