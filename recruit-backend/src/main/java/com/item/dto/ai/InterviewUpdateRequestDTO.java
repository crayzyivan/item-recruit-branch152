package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class InterviewUpdateRequestDTO {
    @JsonProperty(value = "interview_id")
    private String interviewId;
    @JsonProperty(value = "new_time_duration")
    private Integer newTimeDuration;
    private List<String> dimensions;
    private List<String> customQuestions;
    /**
     * 面试类型 (0:video, 1:audio)
     */
    private Integer interviewType;

    /**
     * 检查时间点功能开关
     */
    @JsonProperty("enable_midway_scoring")
    private Boolean enableMidwayScoring;

    /**
     * 检查时间点（分钟）
     */
    @JsonProperty("checkpoint_time_minutes")
    private Integer checkpointTimeMinutes;

    /**
     * 检查时间点分数阈值（0-100）
     */
    @JsonProperty("checkpoint_score_threshold")
    private Integer checkpointScoreThreshold;

    /**
     * 是否仅采用自定义问题
     */
    @JsonProperty("custom_questions_only")
    private Boolean customQuestionOnly;

    /**
     * 是否启用性格测试
     */
    @JsonProperty("enable_personality_test")
    private Boolean enablePersonalityTest;
}
