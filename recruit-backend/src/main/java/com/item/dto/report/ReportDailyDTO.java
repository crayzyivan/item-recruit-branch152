package com.item.dto.report;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
@Data
public class ReportDailyDTO implements Serializable {
    private Long applyCount;
    private Long deniedCount;
    private Long onHoldCount;
    private Long acceptCount;
    private LocalDate createTime;
}
