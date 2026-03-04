package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InterviewUpdateResultVO {
    private Boolean success;
    private String message;
}
