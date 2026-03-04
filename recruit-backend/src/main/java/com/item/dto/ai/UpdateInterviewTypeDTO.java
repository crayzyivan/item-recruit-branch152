package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 更新面试类型请求DTO
 *
 * @author yarong.guo
 * @version 1.0
 * @since 2025-01-27
 */
@Data
public class UpdateInterviewTypeDTO {

    @JsonProperty("interview_id")
    private String interviewId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("interview_type")
    private String interviewType;
}
