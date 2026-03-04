package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
/**
 * 分析结果实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsVO {
    /**
     * Overall score (camelCase format)
     */
    @JsonProperty("overallScore")
    private int overallScore;

    /**
     * Communication analysis
     */
    @JsonProperty("communication")
    private CommunicationVO communication;

    /**
     * Overall feedback (camelCase format)
     */
    @JsonProperty("overallFeedback")
    private String overallFeedback;

    /**
     * Soft skill summary
     */
    @JsonProperty("softSkillSummary")
    private String softSkillSummary;

    /**
     * Dimension analyses (camelCase format)
     */
    @JsonProperty("dimensionAnalyses")
    private List<DimensionAnalysVO> dimensionAnalyses;
}
