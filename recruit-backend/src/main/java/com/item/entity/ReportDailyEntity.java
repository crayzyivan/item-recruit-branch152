package com.item.entity;

import java.io.Serializable;
import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = "r_report_daily")
public class ReportDailyEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String companyCode;
    private Long applyCount = 0L;
    private Long deniedCount = 0L;
    private Long onHoldCount = 0L;
    private Long acceptCount = 0L;
    private LocalDate createTime;
}
