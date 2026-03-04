package com.item.entity.migration.location;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * PostgreSQL companies.locations 表实体类
 * 
 * 映射 PostgreSQL companies.locations 表，用于数据迁移相关操作。
 * 表结构：id (integer primary key), company_id (integer), city (text), 
 * state (text), country (text), postal_code (text), 
 * created_on (timestamp with time zone), updated_on (timestamp with time zone)
 *
 * @author system
 * @since 2025-10-15
 */
@Data
@TableName("companies.locations")
public class PgLocationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 使用序列 locations_id_seq 自动生成
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 公司ID
     * 外键关联 companies.company_data (id)
     */
    @TableField("company_id")
    private Integer companyId;

    /**
     * 城市
     * 对应 PostgreSQL text not null 字段
     */
    @TableField("city")
    private String city;

    /**
     * 州/省
     * 对应 PostgreSQL text not null 字段
     */
    @TableField("state")
    private String state;

    /**
     * 国家
     * 对应 PostgreSQL text not null 字段
     */
    @TableField("country")
    private String country;

    /**
     * 邮政编码
     * 对应 PostgreSQL text 字段（可为空）
     */
    @TableField("postal_code")
    private String postalCode;

    /**
     * 创建时间
     * 对应 PostgreSQL timestamp with time zone not null default now()
     */
    @TableField("created_on")
    private OffsetDateTime createdOn;

    /**
     * 更新时间
     * 对应 PostgreSQL timestamp with time zone
     */
    @TableField("updated_on")
    private OffsetDateTime updatedOn;
}
