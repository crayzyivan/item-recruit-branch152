package com.item.vo;

import lombok.Builder;
import lombok.Data;

/**
 * application列表查询
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-01  10:55
 */
@Data
@Builder
public class ApplicationQueryVO {
    private Integer pageIndex;
    private Integer pageSize;
    private Long jobId;
    private Integer score;
}