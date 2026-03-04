package com.item.schedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.item.entity.JobStatusRecordEntity;
import com.item.entity.ReportDailyEntity;
import com.item.framework.constant.JobApplyStatus;
import com.item.service.JobStatusRecordService;
import com.item.service.ReportService;
import com.item.task.core.handler.annotation.ScheduleTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * <p>
 * 每日报表数据汇总
 * </p>
 *
 * @author liuyabin on 2025/7/24
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReportTask {
    private final JobStatusRecordService jobStatusRecordService;
    private final ReportService reportService;

    @ScheduleTask("dailyReportHandler")
    public void dailyReportHandler() {
        try {
            LocalDate start = LocalDate.now().minusDays(1);
            LocalDate end = LocalDate.now();
            QueryWrapper<JobStatusRecordEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("count(*) as count", "apply_status", "company_code")
                    .lambda()
                    .gt(JobStatusRecordEntity::getCreateTime, start)
                    .lt(JobStatusRecordEntity::getCreateTime, end)
                    .groupBy(JobStatusRecordEntity::getCompanyCode, JobStatusRecordEntity::getApplyStatus);


            List<ReportDailyEntity> reportDailyEntityList = new ArrayList<>();
            Map<String,List<JobStatusRecordEntity>> map = jobStatusRecordService
                    .list(queryWrapper)
                    .stream()
                    .collect(Collectors.groupingBy(JobStatusRecordEntity::getCompanyCode));


            map.entrySet().forEach(entry -> {
                ReportDailyEntity reportDailyEntity = new ReportDailyEntity();
                reportDailyEntity.setCompanyCode(entry.getKey());
                //汇总的日期
                reportDailyEntity.setCreateTime(LocalDate.now().minusDays(1));
                entry.getValue().forEach(r -> {
                    if (r.getApplyStatus().equals(JobApplyStatus.SUBMITTED.getCode())) {
                        reportDailyEntity.setApplyCount(r.getCount());
                    } else if (r.getApplyStatus().equals(JobApplyStatus.DENIED.getCode())) {
                        reportDailyEntity.setDeniedCount(r.getCount());
                    } else if (r.getApplyStatus().equals(JobApplyStatus.REVIEW.getCode()) || r.getApplyStatus().equals(JobApplyStatus.MANUAL_REVIEW.getCode())) {
                        reportDailyEntity.setOnHoldCount(r.getCount());
                    } else if (r.getApplyStatus().equals(JobApplyStatus.ACCEPTED.getCode())) {
                    reportDailyEntity.setApplyCount(r.getCount());
                }

                });
                reportDailyEntityList.add(reportDailyEntity);
            });

            reportService.saveOrUpdateBatch(reportDailyEntityList, reportDailyEntityList.size());
        } catch (Exception ex) {
            log.warn("daily report error ", ex);
        }

    }


}
