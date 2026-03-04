package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * PostgreSQL Applications表实体类
 * 
 * 映射PostgreSQL数据库中的candidates.applications表，
 * 用于存储候选人申请职位的信息，包括申请状态、薪资期望等数据。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Data
@TableName("candidates.applications")
public class ApplicationsEntity {

    /**
     * 主键ID
     * PostgreSQL UUID类型，使用String存储
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 候选人ID
     * 外键关联到candidates.candidate_data表
     */
    @TableField("candidate_id")
    private String candidateId;

    /**
     * 申请标识符
     * 用于唯一标识申请记录
     */
    @TableField("slug")
    private String slug;

    /**
     * 职位ID
     * 外键关联到companies.jobs表
     */
    @TableField("job_id")
    private Integer jobId;

    /**
     * 申请状态
     * PostgreSQL enum_job_application_status枚举类型
     */
    @TableField("status")
    private String status;

    /**
     * 货币ID
     * 关联货币类型
     */
    @TableField("currency_id")
    private Short currencyId;

    /**
     * 期望薪资
     * PostgreSQL numeric类型，使用BigDecimal存储
     */
    @TableField("expected_salary")
    private BigDecimal expectedSalary;

//    /**
//     * 薪资类型
//     * PostgreSQL enum_job_salary_type枚举类型
//     */
//    @TableField("salaryType")
//    private String salaryType;

    /**
     * 可入职时间
     * PostgreSQL timestamptz类型，使用OffsetDateTime处理时区
     */
    @TableField("available_from")
    private OffsetDateTime availableFrom;

    /**
     * 创建时间
     * PostgreSQL timestamptz类型，默认值为now()
     */
    @TableField("created_on")
    private OffsetDateTime createdOn;

    /**
     * 更新时间
     * PostgreSQL timestamptz类型，可为空
     */
    @TableField("updated_on")
    private OffsetDateTime updatedOn;

    /**
     * 创建人ID
     * 外键关联到companies.employee表，可为空
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新人ID
     * 外键关联到companies.employee表，可为空
     */
    @TableField("updated_by")
    private String updatedBy;
}
