package com.item.pgentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * PostgreSQL简历数据实体
 * 对应candidates.resume_data表
 * 
 * UUID字段处理说明：
 * - candidateId和applicationId字段使用String类型存储UUID
 * - 使用UuidUtils工具类进行String与UUID之间的转换
 * - 确保与PostgreSQL数据库的UUID类型兼容
 * 
 * @author system
 */
@Data
@TableName("candidates.resume_data")
public class PgResumeData {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("candidate_id")
    private String candidateId;
    
    @TableField("application_id")
    private String applicationId;
    
    @TableField("file_name")
    private String fileName;
    
    @TableField("created_on")
    private OffsetDateTime createdOn;
}
