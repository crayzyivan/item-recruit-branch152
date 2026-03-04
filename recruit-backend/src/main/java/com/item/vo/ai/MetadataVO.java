package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 元数据实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataVO {
    @JsonProperty("response_id")
    private int responseId;
}
