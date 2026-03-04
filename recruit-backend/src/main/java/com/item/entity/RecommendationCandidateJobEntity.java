package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Recommendation candidate job entity
 * Stores records of job recommendations sent to candidates by recruiters
 *
 * @author hua.liu
 * @since 2025-10-23
 */
@Data
@TableName("r_recommendation_candidate_job")
public class RecommendationCandidateJobEntity {

    /**
     * Primary key ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Job ID
     */
    @TableField("job_id")
    private Long jobId;

    /**
     * Candidate ID
     */
    @TableField("candidate_id")
    private Long candidateId;

    /**
     * Recommender ID (recruiter who recommended this job)
     */
    @TableField("recommend_by")
    private Long recommendBy;

    /**
     * Candidate email address used for sending recommendation
     */
    @TableField("candidate_email")
    private String candidateEmail;

    /**
     * Recommendation time
     */
    @TableField("recommend_time")
    private LocalDateTime recommendTime;

    /**
     * Recommendation reasons
     */
    @TableField("recommend_reasons")
    private String recommendReasons;

    /**
     * Logical delete flag (0: not deleted; 1: deleted)
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    /**
     * Create time
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * Update time
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

