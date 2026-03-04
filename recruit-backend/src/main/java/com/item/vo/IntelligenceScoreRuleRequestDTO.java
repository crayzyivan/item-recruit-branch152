package com.item.vo;

import com.item.framework.annotation.Xss;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 智能评分规则 VO
 * Controller 层使用，用于接收和返回智能评估配置信息
 *
 * @author system
 */
@Data
public class IntelligenceScoreRuleRequestDTO {

    /**
     * 阶段代码
     * 如: SCREENED, AI_VETTED
     */
    @NotBlank(message = "Stage code is required")
    @Xss(message = "Stage code some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String stageCode;

    /**
     * 子阶段代码
     * 如: ASSESSMENT_SCORE, INTERVIEW, PROCTORING, TECHNICAL_SKILLS, SOFT_SKILLS, OVERALL
     */
    @NotBlank(message = "Sub stage code is required")
    @Xss(message = "Sub stage code some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String subStageCode;

    /**
     * 权重 (0-100)
     * 各评估维度在整体评分中的占比
     */
    @NotNull(message = "Weight is required")
    @Min(value = 0, message = "Weight must be at least 0")
    @Max(value = 100, message = "Weight must not exceed 100")
    private Integer weight;

    /**
     * 阈值 (0-100)
     * 通过该阶段的达标分数线
     */
    @NotNull(message = "Threshold is required")
    @Min(value = 0, message = "Threshold must be at least 0")
    @Max(value = 100, message = "Threshold must not exceed 100")
    private Integer threshold;
}
