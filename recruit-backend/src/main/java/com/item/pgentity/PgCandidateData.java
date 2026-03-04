package com.item.pgentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * PostgreSQL候选人数据实体
 * 对应candidates.candidate_data表
 * 
 * UUID字段处理说明：
 * - id、userId、createdBy、updatedBy字段使用String类型存储UUID
 * - 主键使用@TableId(type = IdType.INPUT)配置
 * - 使用UuidUtils工具类进行String与UUID之间的转换
 * - 确保与PostgreSQL数据库的UUID类型兼容
 * 
 * @author system
 */
@Data
@TableName("candidates.candidate_data")
public class PgCandidateData {
    
    @TableId(type = IdType.INPUT)
    private String id;
    
    @TableField("user_id")
    private String userId;
    
    @TableField("slug")
    private String slug;
    
    @TableField("gender")
    private String gender;
    
    @TableField("candidate_dob")
    private LocalDate candidateDob;
    
    @TableField("facebook")
    private String facebook;
    
    @TableField("linkedin")
    private String linkedin;
    
    @TableField("github")
    private String github;
    
    @TableField("address")
    private String address;
    
    @TableField("city")
    private String city;
    
    @TableField("state")
    private String state;
    
    @TableField("country")
    private String country;
    
    @TableField("postal_code")
    private String postalCode;
    
    @TableField("company_id")
    private Integer companyId;
    
    @TableField("created_on")
    private OffsetDateTime createdOn;
    
    @TableField("updated_on")
    private OffsetDateTime updatedOn;
    
    @TableField("created_by")
    private String createdBy;
    
    @TableField("updated_by")
    private String updatedBy;
    
    @TableField("backgroundcheck_user_access_code")
    private String backgroundcheckUserAccessCode;
}
