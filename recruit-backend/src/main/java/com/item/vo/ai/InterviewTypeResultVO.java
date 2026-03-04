package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewTypeResultVO {
    private Boolean success;
    private InterviewTypeDataResultVO data;
}
