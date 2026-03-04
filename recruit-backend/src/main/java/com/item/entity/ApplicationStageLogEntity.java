package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * PostgreSQL ApplicationStageLog表实体类
 *
 * 映射PostgreSQL数据库中的candidates.application_stage_log表，
 * 用于存储候选人申请阶段变更的操作记录。
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Data
@TableName("candidates.application_stage_log")
public class ApplicationStageLogEntity {

    /**
     * 主键ID
     * PostgreSQL int4类型，使用Integer存储
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 申请ID
     * 外键关联到candidates.applications表
     * PostgreSQL UUID类型，使用String存储
     */
    @TableField("application_id")
    private String applicationId;

    /**
     * 阶段ID
     * 表示申请所处的阶段
     * PostgreSQL int2类型，使用Short存储
     */
    @TableField("stage_id")
    private Short stageId;

    /**
     * 更新时间
     * PostgreSQL timestamptz类型，默认值为now()
     * 使用OffsetDateTime处理时区
     */
    @TableField("updated_on")
    private OffsetDateTime updatedOn;

    /**
     * 更新人ID
     * 外键关联到companies.employee表，可为空
     * PostgreSQL UUID类型，使用String存储
     */
    @TableField("updated_by")
    private String updatedBy;
}
