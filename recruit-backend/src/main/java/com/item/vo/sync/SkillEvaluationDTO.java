package com.item.vo.sync;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * ai技能评估结果
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-29  11:54
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillEvaluationDTO {
    private String skill;
    //private Double timestamp;

    @JsonProperty("ai_evaluation")
    private AiEvaluationDTO aiEvaluation;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiEvaluationDTO {
        private String rating;
        private String feedback;
    }
}