package com.item.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 人工审核列表查询
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-23  10:55
 */
@Data
@Builder
public class PendingReviewQueryVO {
    private Integer pageIndex;
    private Integer pageSize;
    private Long jobId;
    private Integer score;
}