package com.item.dto.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * pdf生成工作描述响应参数
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-05  16:41
 */
@Data
public class JobDecResponseDTO implements Serializable {
    private Integer code;
    private JobDescriptionDTO data;

}