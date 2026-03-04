package com.item.vo.report;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 *  首页顶部汇总数据汇总
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
@Data
public class TopViewDataVO implements Serializable {
    private Long openJobTotalCount;
    /**
     * 候选人总数
     */
    private Long candidateTotalCount;
    private Long pendingReviewTotalCount;
    private Long deniedTotalCount;
    private Long readyTotalCount;
}
