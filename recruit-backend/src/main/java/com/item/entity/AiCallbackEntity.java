package com.item.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("r_ai_callback")
public class AiCallbackEntity {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 回调事件
     */
    private String event;

    /**
     * 事件AI处理完成时间
     */
    private LocalDateTime timestamp;

    /**
     * AI面试ID
     */
    private String interviewId;

    /**
     * 面试者邮箱
     */
    private String interviewerEmail;

    /**
     * 面试者电话
     */
    private String interviewerPhone;

    /**
     * 面试者面试ID
     */
    private String callId;

    /**
     * 任务处理状态（-1：处理失败，0：未处理，1处理中，2：已处理）
     */
    private Integer status;

    private Long candidateJobId;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
