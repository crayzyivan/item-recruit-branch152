package com.item.vo.report;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/23
 * @since 1.0.0
 */
@Data
public class ReportDailyVO implements Serializable {
    private Long applyCount;
    private Long deniedCount;
    private Long onHoldCount;
    private Long acceptCount;
    private LocalDate createTime;
}