package com.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ai面试结果页面 通过操作
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-23  18:48
 */
@Data
public class VettedPassDto {

    @NotNull(message = "ID cannot be null")
    private Long id;

}