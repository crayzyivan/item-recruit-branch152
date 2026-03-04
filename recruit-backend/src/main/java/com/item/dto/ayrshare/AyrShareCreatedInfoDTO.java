package com.item.dto.ayrshare;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Ayrshare创建时间信息DTO
 *
 * @author lh
 */
@Data
public class AyrShareCreatedInfoDTO {
    
    /**
     * 秒数
     */
    @JsonProperty("_seconds")
    private Long seconds;
    
    /**
     * 纳秒数
     */
    @JsonProperty("_nanoseconds")
    private Integer nanoseconds;
    
    /**
     * UTC时间
     */
    private String utc;
}
