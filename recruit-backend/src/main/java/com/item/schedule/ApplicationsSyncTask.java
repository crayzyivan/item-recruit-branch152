package com.item.schedule;

import com.item.entity.ApplicationsEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.service.ApplicationsService;
import com.item.service.ApplicationsSyncService;
import com.item.service.migration.DataMigrationMappingService;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Applications同步定时任务
 * 
 * 定时同步PostgreSQL candidates.applications表到MySQL r_candidate_job表，
 * 实现全量数据同步，确保数据一致性。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ApplicationsSyncTask {

    private final ApplicationsService applicationsService;
    private final ApplicationsSyncService applicationsSyncService;
    private final DataMigrationMappingService dataMigrationMappingService;
    private final RedisTemplate<String,String> redisTemplate;

    @ScheduleTask("applicationsSyncTask")
    public void sync() {
        String redisKey="applicationsSyncTask";
        try {
            log.info("ApplicationsSyncTask: start");
            // 1. 查询PostgreSQL中所有Applications记录
            List<ApplicationsEntity> applications = applicationsService.listByUpdateStartDate(null);
            if (CollectionUtils.isEmpty(applications)) {
                log.info("ApplicationsSyncTask: no applications found for sync");
                return;
            }
            log.info("ApplicationsSyncTask: found {} applications to sync", applications.size());
            // 2. 遍历每条记录进行同步
            // 分批处理
            int batchSize = 8;
            for (int i = 0; i < applications.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, applications.size());
                List<ApplicationsEntity> batch = applications.subList(i, endIndex);
                log.info("applicationsSyncTask Processing batch start {}-{} of {}", i + 1, endIndex, applications.size());

                // 并行处理当前批次
                List<CompletableFuture<Boolean>> futures = batch.stream()
                        .map(application -> CompletableFuture.supplyAsync(() -> {
                            return applicationsSyncService.syncSingleApplication(application);
                        }))
                        .collect(Collectors.toList());
                // 等待当前批次完成
                CompletableFuture<Void> batchFuture = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));
                batchFuture.join();
                log.info("applicationsSyncTask Processing batch end {}-{} of {}", i + 1, endIndex, applications.size());
            }
            log.info("ApplicationsSyncTask: sync completed.");
        } catch (Exception e) {
            log.error("ApplicationsSyncTask: sync failed", e);
        }
    }
}
