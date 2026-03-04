package com.item.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("r_candidate_recall")
public class CandidateRecallEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("candidate_job_id")
    private Long candidateJobId;

    private String preferredInterviewStartTime;

    private String preferredInterviewEndTime;

    private String interviewPhone;

    private String interviewLanguage;

    private String scheduleId;

    /**
     * 短信是否发送成功（0：失败，1：成功）
     */
    private Integer smsStatus;

    /**
     * 是否回拨成功（0：未回拨，1：已回拨）
     */
    private Integer recallStatus;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

}
