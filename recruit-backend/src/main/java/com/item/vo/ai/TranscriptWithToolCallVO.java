package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
/**
 * 包含工具调用的转录内容实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscriptWithToolCallVO {
    @JsonProperty("role")
    private String role;

    @JsonProperty("words")
    private List<WordVO> words;

    @JsonProperty("content")
    private String content;

    @JsonProperty("metadata")
    private MetadataVO metadata;

    @JsonProperty("name")
    private String name;

    @JsonProperty("arguments")
    private String arguments;

    @JsonProperty("tool_call_id")
    private String toolCallId;

}
