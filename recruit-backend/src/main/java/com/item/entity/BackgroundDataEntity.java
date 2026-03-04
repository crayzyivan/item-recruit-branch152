package com.item.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>
 * 背调数据实体类
 * </p>
 *
 * @author liuyabin on 2025/7/31
 * @since 1.0.0
 */
@Data
@TableName(value = "r_bg_data")
public class BackgroundDataEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long candidateJobId;
    private Long candidateId;
    private String candidateName;
    private String firstName;
    private String lastName;
    private String middleName;
    private Long jobId;
    private String jobTitle;
    private String email;
    /**
     * 候选人申请日期
     */
    private LocalDateTime applicationDate;
    /**
     *  背调状态 0--pending,1--completed,2--failed
     */
    @TableField(value = "bg_status")
    private int backgroundStatus;
    /**
     * 背调触发日期
     */
    @TableField(value = "bg_date")
    private LocalDateTime backgroundDate;
    /**
     * 犯罪记录，0--无,1--有记录
     */
    @TableField(value = "criminal_record")
    private LocalDateTime criminalRecord;
    /**
     * 过往工作经历是否验证
     */
    @TableField(value = "employment_history_verified")
    private boolean employmentHistoryVerified;
    @TableField(value = "edu_verified")
    private boolean educationVerified;
    private String reportUrl;
    private String feedbackSummary;
    private String userAccessCode;
    private LocalDateTime expires;
    private String reportStage;
}
