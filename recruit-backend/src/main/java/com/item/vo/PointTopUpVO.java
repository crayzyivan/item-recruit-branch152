package com.item.vo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分充值请求参数
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  19:04
 */
@Data
public class PointTopUpVO {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1", message = "Amount must be greater than or equal to {value}")
    private BigDecimal amount;

    private String remark;
}