package com.item.entity.migration.job;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * PostgreSQL jobs 表实体类
 * 
 * 映射 PostgreSQL companies.jobs 表，用于数据迁移到 MySQL r_job 表。
 * 支持 JSONB details 字段和 category_ids 数组字段的处理。
 *
 * @author system
 * @since 2025-09-30
 */
@Data
@TableName("companies.jobs")
public class PgJobEntity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * URL slug
     */
    @TableField("slug")
    private String slug;

    /**
     * 职位标题
     */
    @TableField("title")
    private String title;

    /**
     * 公司ID
     */
    @TableField("company_id")
    private Integer companyId;

    /**
     * 职位状态
     * PostgreSQL 枚举类型: enum_job_status
     */
    @TableField("status")
    private String status;

    /**
     * 位置ID
     */
    @TableField("location_id")
    private Integer locationId;

    /**
     * 职位空缺数量
     */
    @TableField("number_of_openings")
    private Integer numberOfOpenings;

    /**
     * 职位类型
     * PostgreSQL 枚举类型: enum_job_type
     */
    @TableField("type")
    private String type;

    /**
     * 位置类型
     * PostgreSQL 枚举类型: enum_job_location_type
     */
    @TableField("\"locationType\"")
    @JsonProperty("locationType")
    private String locationType;

    /**
     * 薪资类型
     * PostgreSQL 枚举类型: enum_job_salary_type
     */
    @TableField("\"salaryType\"")
    @JsonProperty("salaryType")
    private String salaryType;

    /**
     * 货币ID
     */
    @TableField("currency_id")
    private Short currencyId;

    /**
     * 最小薪资
     */
    @TableField("min_salary")
    private BigDecimal minSalary;

    /**
     * 最大薪资
     */
    @TableField("max_salary")
    private BigDecimal maxSalary;

    /**
     * 职位详情 (JSONB 字段)
     * 包含 overview、skills、benefits、responsibilities、requirements 等信息
     */
    @TableField("details")
    private String details;

    /**
     * 是否为热门职位
     */
    @TableField("hotlist")
    private Boolean hotlist;

    /**
     * 创建时间
     */
    @TableField("created_on")
    private OffsetDateTime createdOn;

    /**
     * 创建人ID (UUID)
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField("updated_on")
    private OffsetDateTime updatedOn;

    /**
     * 更新人ID (UUID)
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 自定义问题 (字符串数组)
     * PostgreSQL text[] 类型
     */
    @TableField("custom_questions")
    private String customQuestions;

    /**
     * 面试ID
     */
    @TableField("interview_id")
    private String interviewId;

    /**
     * 分类ID数组
     * PostgreSQL integer[] 类型
     */
    @TableField(value = "category_ids")
    private String categoryIds;
}
