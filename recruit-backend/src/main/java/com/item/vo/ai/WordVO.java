package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 单词信息实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordVO {
    @JsonProperty("end")
    private double end;

    @JsonProperty("word")
    private String word;

    @JsonProperty("start")
    private double start;

}
