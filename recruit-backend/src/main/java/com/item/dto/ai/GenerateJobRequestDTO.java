package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;


/**
 * <p>
 * 生成岗位请求
 * </p>
 *
 * @author liuyabin on 2025/7/19
 * @since 1.0.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerateJobRequestDTO implements Serializable {
    private String jobTitle;
    private String extraInfo;
    private LocationAIDTO location;
}
