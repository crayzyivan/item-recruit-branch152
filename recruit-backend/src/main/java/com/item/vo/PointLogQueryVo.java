package com.item.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 积分操作记录查询参数
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-15  17:45
 */
@Data
@Builder
public class PointLogQueryVo {
    private Integer pageIndex;
    private Integer pageSize;
    private String transactionNo;
    private Integer transactionType;
    private Integer pointStatus;
    private Long userId;
}