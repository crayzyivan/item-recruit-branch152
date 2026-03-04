package com.item.dto.job;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CandidateInterviewTimeRequestVO {
    /**
     * 候选人ID
     * 必填，不能为空
     */
    @NotNull(message = "Candidate ID is required")
    private Long candidateId;
    /**
     * 候选人期望 AI 电话面试开始时间，如：2025-12-11T17:00:00+08:00
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime preferredInterviewStartTime;

    /**
     * 候选人期望 AI 电话面试结束时间，如：2025-12-11T18:00:00+08:00
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime preferredInterviewEndTime;
}
