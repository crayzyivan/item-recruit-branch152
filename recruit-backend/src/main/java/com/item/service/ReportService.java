package com.item.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.report.ReportDailyDTO;
import com.item.dto.report.TopReportDTO;
import com.item.entity.ReportDailyEntity;
import com.item.framework.constant.ReportType;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
public interface ReportService extends IService<ReportDailyEntity> {

    /**
     * 获取每日数据报告
     * @param companyCode
     * @param type
     * @return
     */
    List<ReportDailyDTO> getDailyReport(String companyCode, ReportType type);

    /**
     * 获取首页数据汇总
     * @param companyCode
     * @return
     */
    TopReportDTO getTopReport(String companyCode);
}
