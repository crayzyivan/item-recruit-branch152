package com.item.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分操作记录列表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-15  17:42
 */
@Data
public class PointLogListVo {
    private Long id;
    private String transactionNo;
    private Integer transactionType;
    private BigDecimal amount;
    private Integer points;
    private Integer pointStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer pricingModel;
    private Integer durationMinutes;
    private Integer consumedTokens;
    private String errorMessage;
    private String companyCode;
}