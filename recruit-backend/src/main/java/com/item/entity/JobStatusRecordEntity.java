package com.item.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 招聘状态流转记录
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  17:15
 */
@Data
@TableName("r_job_status_record")
public class JobStatusRecordEntity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "candidate_job_id")
    private Long candidateJobId;

    @TableField(value = "company_code")
    private String companyCode;

    @TableField(value = "old_apply_status")
    private Integer oldApplyStatus;

    @TableField(value = "apply_status")
    private Integer applyStatus;

    @TableField(value = "apply_event")
    private String applyEvent;

    @TableField(value = "create_by")
    private String createBy;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false, value = "count(*)",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private Long count;

}