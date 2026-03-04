package com.item.schedule;

import com.item.entity.ApplicationInterviewReportsEntity;
import com.item.service.ApplicationInterviewReportsService;
import com.item.service.InterviewResultSyncService;
import com.item.task.core.handler.annotation.ScheduleTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * 面试结果同步定时任务
 * 
 * 定期从PostgreSQL数据库同步面试结果数据到MySQL数据库。
 * 该任务会查询所有未同步的面试报告，并批量同步到MySQL的r_ai_vetted_result表中。
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-09
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class InterviewResultSyncTask {

    private final InterviewResultSyncService interviewResultSyncService;
    private final ApplicationInterviewReportsService applicationInterviewReportsService;
    private final RedisTemplate<String,String> redisTemplate;

    /**
     * 面试结果同步定时任务主方法
     * 
     * 该方法执行以下步骤：
     * 查询PostgreSQL中所有的面试报告数据
     * 遍历每个面试报告，调用同步服务进行数据同步
     */
    @ScheduleTask("interviewResultSyncTask")
    public void syncInterviewResults() {
        String redisKey="interviewResultSyncTask";
        try {
            log.info("Interview result sync task started at: {}", LocalDateTime.now());
            // 查询所有面试报告数据
            List<ApplicationInterviewReportsEntity> reports = applicationInterviewReportsService.listByStartDate(null);
            log.info("Found {} interview reports to process", reports.size());
            
            if (CollectionUtils.isEmpty(reports)) {
                log.info("No interview reports found for synchronization");
                return;
            }

            // 遍历每个面试报告进行同步
            for (ApplicationInterviewReportsEntity report : reports) {
                try {
                    log.info("Interview result sync task candidateJobId:{},applicationId:{}",report.getId(),report.getApplicationId());
                    interviewResultSyncService.syncInterviewResultFromPg(report.getId(),report.getApplicationId());
                } catch (Exception e) {
                    log.error("Failed to sync interview result for reportId: {}, applicationId: {}, error: {}",
                            report.getId(), report.getApplicationId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Interview result sync task failed with unexpected error", e);
        }
    }
}
