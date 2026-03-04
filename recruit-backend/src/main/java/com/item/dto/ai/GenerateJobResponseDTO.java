package com.item.dto.ai;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/19
 * @since 1.0.0
 */
@Data
public class GenerateJobResponseDTO implements Serializable {
    private Integer code;
    private JobResponseDataDTO data;
}
