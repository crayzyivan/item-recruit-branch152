package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class InterviewRequestDTO {
    private String organizationName;
    private InterviewDataDTO interviewData;
    private QuestionGenerationDTO questionGeneration;
    private List<String> customQuestions;
    /**
     * 检查时间点功能开关
     */
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
    private Boolean customQuestionsOnly;

    /**
     * 是否启用性格测试
     */
    private Boolean enablePersonalityTest;
}
