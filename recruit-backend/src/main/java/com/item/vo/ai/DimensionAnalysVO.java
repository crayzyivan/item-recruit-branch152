package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 问题总结实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DimensionAnalysVO {
    @JsonProperty("score")
    private int score;

    @JsonProperty("feedback")
    private String feedback;

    @JsonProperty("dimension")
    private String dimension;

    @JsonProperty("questionSummaries")
    private List<QuestionSummaryVO> questionSummaries;

    /**
     * Questions evaluation list
     */
    private List<QuestionVO> questions;

}
