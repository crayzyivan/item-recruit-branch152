package com.item.dto.report;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 顶部数据汇总
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
@Data
public class TopReportDTO implements Serializable {
    private Long openJobTotalCount;
    private Long applicationTotalCount;
    private Long pendingReviewCount;
    private Long deniedTotalCount;
    private Long readyTotalCount;
}
