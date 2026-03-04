package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheatingAnalysisVO {
    private String reason;
    private String timestamp;
    private Float probability;
}
