package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Job approval email retry tracking entity
 * 
 * @author system
 * @since 1.0.0
 */
@Data
@TableName(value = "r_job_approval_email_retry")
public class JobApprovalEmailRetryEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "job_id")
    private Long jobId;

    @TableField(value = "action")
    private String action;

    @TableField(value = "comment")
    private String comment;

    @TableField(value = "template_name")
    private String templateName;

    @TableField(value = "variables")
    private String variables; // JSON string

    @TableField(value = "admin_email")
    private String adminEmail;

    @TableField(value = "retry_count")
    private Integer retryCount;

    @TableField(value = "status")
    private String status; // PENDING, COMPLETED, FAILED

    @TableField(value = "first_failure_time")
    private LocalDateTime firstFailureTime;

    @TableField(value = "last_retry_time")
    private LocalDateTime lastRetryTime;

    @TableField(value = "completed_time")
    private LocalDateTime completedTime;

    @TableField(value = "deleted")
    private Boolean deleted;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "create_by_id")
    private Long createById;

    @TableField(value = "update_by_id")
    private Long updateById;
}
