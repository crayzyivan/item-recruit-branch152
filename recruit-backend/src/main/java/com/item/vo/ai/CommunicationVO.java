package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 沟通评分实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunicationVO {
    @JsonProperty("score")
    private int score;

    @JsonProperty("feedback")
    private String feedback;

}
