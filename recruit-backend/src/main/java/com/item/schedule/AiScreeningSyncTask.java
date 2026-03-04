package com.item.schedule;

import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.service.AiScreeningSyncService;
import com.item.service.ApplicationScreeningReportsService;
import com.item.service.migration.DataMigrationMappingService;
import com.item.task.core.handler.annotation.ScheduleTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI筛选结果同步定时任务
 *
 * 定期执行AI筛选结果同步操作，从PostgreSQL的application_screening_reports表
 * 查询新的筛选结果并同步到MySQL的r_candidate_job表和ES索引。
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Component
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
@RequiredArgsConstructor
public class AiScreeningSyncTask {

    private final AiScreeningSyncService aiScreeningSyncService;
    private final DataMigrationMappingService dataMigrationMappingService;
    private final ApplicationScreeningReportsService applicationScreeningReportsService;
    private final StringRedisTemplate redisTemplate;

    @ScheduleTask("aiScreeningSyncHandler")
    public void aiScreeningSyncTask() {
        List<DataMigrationMappingEntity> mappingEntityList = dataMigrationMappingService.findAllByTypeAndDate(MigrationBusTypeEnum.CANDIDATE_JOB,null);
        if (CollectionUtils.isNotEmpty(mappingEntityList)) {
            log.info("AI screening sync task found mappingEntityList: {}", mappingEntityList);
            List<String> allApplicationIds = applicationScreeningReportsService.listAllApplicationIds();

            // 分批处理
            int batchSize = 8;
            for (int i = 0; i < mappingEntityList.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, mappingEntityList.size());
                List<DataMigrationMappingEntity> batch = mappingEntityList.subList(i, endIndex);
                log.info("Processing batch start {}-{} of {}", i + 1, endIndex, mappingEntityList.size());

                // 并行处理当前批次
                List<CompletableFuture<Boolean>> futures = batch.stream()
                        .map(mapping -> CompletableFuture.supplyAsync(() -> {
                            if (allApplicationIds.contains(mapping.getPgsqlId())) {
                                return aiScreeningSyncService.syncScreeningResult(mapping.getMysqlId(), mapping.getPgsqlId());
                            }
                            return true;
                        }))
                        .collect(Collectors.toList());
                 // 等待当前批次完成
                CompletableFuture<Void> batchFuture = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));
                batchFuture.join();

                log.info("Processing batch end {}-{} of {}", i + 1, endIndex, mappingEntityList.size());
            }
        }
        log.info("AI screening sync task end");
    }
}
