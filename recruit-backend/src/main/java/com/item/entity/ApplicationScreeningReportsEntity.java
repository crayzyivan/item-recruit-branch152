package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * AI筛选报告表实体类
 * 
 * 映射PostgreSQL数据库中的candidates.application_screening_reports表，
 * 用于存储AI筛选系统生成的筛选报告和评分数据。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Data
@TableName("candidates.application_screening_reports")
public class ApplicationScreeningReportsEntity {

    /**
     * 主键ID
     * 对应数据库字段：id (int4 NOT NULL DEFAULT nextval)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的申请ID
     * 对应数据库字段：application_id (uuid NOT NULL)
     * 外键关联到candidates.applications表
     */
    @TableField("application_id")
    private String applicationId;

    /**
     * 筛选日期
     * 对应数据库字段：screening_date (timestamptz(6) NOT NULL DEFAULT now())
     */
    @TableField("screening_date")
    private OffsetDateTime screeningDate;

    /**
     * 筛选概览（JSON格式）
     * 对应数据库字段：overview (jsonb NOT NULL)
     * PostgreSQL JSONB类型，存储复杂的筛选结果数据
     */
    @TableField("overview")
    private String overview;

    /**
     * 评估分数
     * 对应数据库字段：assessment_score (int2 NOT NULL)
     * AI筛选的评估分数
     */
    @TableField("assessment_score")
    private Integer assessmentScore;

    /**
     * 推荐结果
     * 对应数据库字段：recommendation (text NOT NULL)
     * AI筛选的推荐结果，如"Deny Outright"、"Proceed with Application"等
     */
    @TableField("recommendation")
    private String recommendation;

    /**
     * 筛选评论
     * 对应数据库字段：comments (text NOT NULL)
     * AI筛选的详细评论和说明
     */
    @TableField("comments")
    private String comments;
}
