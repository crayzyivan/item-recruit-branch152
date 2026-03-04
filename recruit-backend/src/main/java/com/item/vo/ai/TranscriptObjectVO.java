package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
/**
 * 转录内容对象实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscriptObjectVO {
    @JsonProperty("role")
    private String role;

    @JsonProperty("words")
    private List<WordVO> words;

    @JsonProperty("content")
    private String content;

    @JsonProperty("metadata")
    private MetadataVO metadata;
}
