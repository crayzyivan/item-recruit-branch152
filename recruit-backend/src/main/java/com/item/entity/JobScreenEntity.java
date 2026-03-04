package com.item.entity;

import java.io.Serializable;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 *  职位候选人表
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
@Data
@TableName(value = "r_screen_job")
public class JobScreenEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private long id;
    @TableField(value = "job_id")
    private long jobId;
    @TableField(value = "candidate_id")
    private long candidateId;
    private int score;
    @TableField(value = "process_status")
    private int processStatus;
    @TableField(value = "customer_id")
    private long customerId;
}
