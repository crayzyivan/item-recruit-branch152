package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InterviewResultVO {
    private String response;
    @JsonProperty("url_id")
    private String urlId;
}
