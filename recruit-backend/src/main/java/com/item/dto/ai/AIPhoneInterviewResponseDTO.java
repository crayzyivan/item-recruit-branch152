package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AIPhoneInterviewResponseDTO {

    @JsonProperty("success")
    private boolean success;
}
