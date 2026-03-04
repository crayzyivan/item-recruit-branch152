package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "r_job_approval_settings")
public class JobApprovalSettingsEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "company_code")
    private String companyCode;

    @TableField(value = "approval_required")
    private Boolean approvalRequired;

    @TableField(value = "email_notifications")
    private Boolean emailNotifications;

    @TableField(value = "admin_email")
    private String adminEmail;

    @TableLogic
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
