package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分操作记录表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  09:11
 */
@Data
@TableName("r_points_operation_log")
public class PointsOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;

    @TableField("oper_user_id")
    private Long operUserId;

    @TableField("company_code")
    private String companyCode;
    /**
     * 交易编号  可根据 RedisSerialNumberUtils,使用TransactionTypeEnum生成相对应类型的交易编号
     */
    @TableField("transaction_no")
    private String transactionNo;
    /**
     * 交易类型  TransactionTypeEnum
     */
    @TableField("transaction_type")
    private Integer transactionType;
    /**
     * 交易金额  充值使用
     */
    @TableField("amount")
    private BigDecimal amount;
    /**
     * 交易积分  正为增加、减为扣分
     */
    @TableField("points")
    private Integer points;
    /**
     * 积分状态  PointStatusEnum
     */
    @TableField("point_status")
    private Integer pointStatus;
    /**
     *
     */
    @TableField("freeze_action_type")
    private Integer freezeActionType;
    @TableField("job_id")
    private Long jobId;
    @TableField("candidate_id")
    private Long candidateId;
    @TableField("candidate_job_id")
    private Long candidateJobId;
    @TableField("remark")
    private String remark;
    @TableField("retry_count")
    private Integer retryCount;
    @TableField("error_message")
    private String errorMessage;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
    @TableField("pricing_model")
    private Integer pricingModel;
    @TableField("duration_seconds")
    private Integer durationSeconds;

    @TableField("duration_minutes")
    private Integer durationMinutes;
    @TableField("consumed_tokens")
    private Integer consumedTokens;

    /**
     * freeze interview type (0:video, 1:audio)
     */
    @TableField("freeze_interview_type")
    private Integer freezeInterviewType;

    /**
     * actual interview type (0:video, 1:audio)
     */
    @TableField("actual_interview_type")
    private Integer actualInterviewType;

}