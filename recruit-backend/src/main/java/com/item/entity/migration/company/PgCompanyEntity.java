package com.item.entity.migration.company;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * PostgreSQL companies.company_data 表实体类
 * 
 * 映射 PostgreSQL companies.company_data 表，用于读取公司的 ayrshare_profile_key 配置信息。
 * 主要用于 ayrshare 配置数据迁移到 MySQL r_ayrshare_company_config 表。
 *
 * @author system
 * @since 2025-10-15
 */
@Data
@TableName("companies.company_data")
public class PgCompanyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 使用序列 company_data_id_seq 自动生成
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 公司名称
     * 对应 PostgreSQL text not null 字段
     */
    @TableField("name")
    private String name;

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

    /**
     * 公司网站
     * 对应 PostgreSQL text 字段（可为空）
     */
    @TableField("website")
    private String website;

    /**
     * 公司 Logo
     * 对应 PostgreSQL text 字段（可为空）
     */
    @TableField("logo")
    private String logo;

    /**
     * Ayrshare Profile Key
     * 存储在 company_data 表中的 ayrshare 配置信息
     * 对应 PostgreSQL text 字段（可为空）
     */
    @TableField("ayrshare_profile_key")
    private String ayrshareProfileKey;

    /**
     * LinkedIn 公司 ID
     * 对应 PostgreSQL text 字段（可为空）
     */
    @TableField("linkedin_company_id")
    private String linkedinCompanyId;
}

