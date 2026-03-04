package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("r_job_audit_history")
public class JobAuditHistoryEntity implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("job_id")
    private Long jobId;
    
    @TableField("old_status")
    private Integer oldStatus;
    
    @TableField("new_status")
    private Integer newStatus;
    
    @TableField("action")
    private String action;
    
    @TableField("comment")
    private String comment;
    
    @TableField("created_by")
    private Long createdBy;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}

