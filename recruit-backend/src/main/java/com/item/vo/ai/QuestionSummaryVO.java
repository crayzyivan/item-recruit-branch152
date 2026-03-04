package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 问题总结实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionSummaryVO {
    @JsonProperty("summary")
    private String summary;

    @JsonProperty("question")
    private String question;

}
