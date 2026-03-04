package com.item.schedule;

import com.item.entity.XmlFeedConfigEntity;
import com.item.entity.XmlFeedUpdateLogEntity;
import com.item.framework.constant.XmlFeedConstants;
import com.item.service.XmlFeedConfigService;
import com.item.service.XmlFeedService;
import com.item.service.XmlFeedUpdateLogService;
import com.item.task.core.handler.annotation.ScheduleTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * XML Feed auto update scheduled task
 * 
 * This task automatically processes XML feed updates when the configured next_update_time is reached.
 * It queries for expired configurations, generates XML feeds in parallel using thread pool (max 5 concurrent),
 * and logs the update results. Uses aiTaskExecutor with Semaphore to control concurrency.
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XmlFeedScheduler {

    private final XmlFeedConfigService xmlFeedConfigService;
    private final XmlFeedUpdateLogService xmlFeedUpdateLogService;
    private final XmlFeedService xmlFeedService;
    private final ThreadPoolTaskExecutor aiTaskExecutor;
    
    // Semaphore to limit concurrent processing to 5 threads
    private final Semaphore processingSemaphore = new Semaphore(5);

    /**
     * Auto update XML feeds for expired configurations using parallel processing
     * 
     * This method:
     * 1. Queries configurations where next_update_time <= current time and deleted = false
     * 2. Processes configurations in parallel using aiTaskExecutor thread pool (max 5 concurrent)
     * 3. For each configuration: creates update log entries, generates XML feeds, updates timestamps
     * 4. Uses Semaphore to control concurrent processing and CompletableFuture for async execution
     * 5. Waits for all tasks to complete and logs final success/failure statistics
     */
    @ScheduleTask("xmlFeedAutoUpdateHandler")
    public void xmlFeedAutoUpdateHandler() {
        LocalDateTime currentTime = LocalDateTime.now();
        log.info("XML Feed auto update task started at: {}", currentTime);
        
        try {
            // Query expired configurations
            List<XmlFeedConfigEntity> expiredConfigs = xmlFeedConfigService.findExpiredConfigurations(currentTime);

            log.info("Found {} expired XML feed configurations to update", expiredConfigs.size());

            // Process expired configurations in parallel using thread pool (max 5 concurrent)
            if (CollectionUtils.isNotEmpty(expiredConfigs)){
                processExpiredConfigurationsInParallel(expiredConfigs);
            }
        } catch (Exception e) {
            log.error("XML Feed auto update task failed with unexpected error", e);
        }
        log.info("XML Feed auto update task end at: {}", LocalDateTime.now());
    }
    
    /**
     * Process expired configurations in parallel using thread pool
     * 
     * @param expiredConfigs list of expired configurations to process
     */
    private void processExpiredConfigurationsInParallel(List<XmlFeedConfigEntity> expiredConfigs) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // Create CompletableFuture tasks for each configuration
        CompletableFuture<?>[] futures = expiredConfigs.stream()
            .map(config -> CompletableFuture.runAsync(() -> {
                try {
                    // Acquire semaphore permit to limit concurrent processing to 5 threads
                    processingSemaphore.acquire();
                    try {
                        processExpiredConfiguration(config);
                        successCount.incrementAndGet();
                        log.info("Successfully processed XML feed auto update for config ID: {}, company: {}, platform: {}", 
                                config.getId(), config.getCompanyCode(), 
                                XmlFeedConstants.PlatformType.getNameByCode(config.getPlatformType()));
                    } finally {
                        // Release semaphore permit
                        processingSemaphore.release();
                    }
                } catch (InterruptedException e) {
                    failureCount.incrementAndGet();
                    Thread.currentThread().interrupt();
                    log.error("Thread interrupted while processing config ID: {}, company: {}, platform: {}", 
                             config.getId(), config.getCompanyCode(), 
                             XmlFeedConstants.PlatformType.getNameByCode(config.getPlatformType()), e);
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    log.error("Failed to process XML feed auto update for config ID: {}, company: {}, platform: {}, error: {}", 
                             config.getId(), config.getCompanyCode(), 
                             XmlFeedConstants.PlatformType.getNameByCode(config.getPlatformType()), 
                             e.getMessage(), e);
                }
            }, aiTaskExecutor))
            .toArray(CompletableFuture[]::new);
        
        // Wait for all tasks to complete
        CompletableFuture.allOf(futures).join();
        
        log.info("XML Feed auto update task completed. Success: {}, Failure: {}", 
                successCount.get(), failureCount.get());
    }

    
    /**
     * Process a single expired configuration
     * 
     * @param config expired configuration to process
     * @throws Exception if processing fails
     */
    private void processExpiredConfiguration(XmlFeedConfigEntity config) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        Integer jobCount = null;
        try {
            // Generate XML feed based on platform type
            jobCount = generateXmlFeedByPlatform(config);
            // Update configuration timestamps
            updateConfigurationTimestamps(config);
            // Record successful update
            recordUpdateLog(config.getId(), XmlFeedConstants.UpdateStatus.SUCCESS.getCode(), startTime, jobCount, null);
        } catch (Exception e) {
            // Record failure in the log entry
            recordUpdateLog(config.getId(), XmlFeedConstants.UpdateStatus.FAILED.getCode(), startTime, jobCount, e.getMessage());
            // Re-throw exception to be handled by parallel processing
            throw e;
        }
    }

    /**
     * Generate XML feed based on platform type
     * 
     * @param config configuration containing platform type and company code
     */
    private Integer generateXmlFeedByPlatform(XmlFeedConfigEntity config) {
        String companyCode = config.getCompanyCode();
        Integer platformType = config.getPlatformType();
        if (XmlFeedConstants.PlatformType.LINKEDIN.getCode().equals(platformType)) {
            return xmlFeedService.generateLinkedInXML(companyCode);
        } else if (XmlFeedConstants.PlatformType.INDEED.getCode().equals(platformType)) {
            return xmlFeedService.generateIndeedXML(companyCode,config.getAccountEmail());
        }else if (XmlFeedConstants.PlatformType.ZIP_RECRUITER.getCode().equals(platformType)) {
            return xmlFeedService.generateZipRecruiterXML(companyCode,config.getAccountEmail());
        }
        return null;
    }
    
    /**
     * Update configuration timestamps after successful XML generation
     * 
     * @param config configuration to update
     */
    private void updateConfigurationTimestamps(XmlFeedConfigEntity config) {
        LocalDateTime now = LocalDateTime.now();
        
        // Update last update time and calculate next update time
        config.setLastUpdateTime(now);
        config.setNextUpdateTime(now.plusHours(config.getUpdateIntervalHours()));
        
        // Save updated configuration
        xmlFeedConfigService.updateConfigEntity(config);
    }

    /**
     * Record failed update in log table (for configuration without existing log)
     * 
     * @param feedConfigId failed configuration
     * @param errorMessage error message
     */
    private void recordUpdateLog(Long feedConfigId,Integer updateStatus,LocalDateTime startTime,Integer jobCount,String errorMessage) {
        XmlFeedUpdateLogEntity failureLog = new XmlFeedUpdateLogEntity();
        failureLog.setFeedConfigId(feedConfigId);
        failureLog.setUpdateType(XmlFeedConstants.UpdateType.AUTO.getCode());
        failureLog.setUpdateStatus(updateStatus);
        failureLog.setOperatorId(null);
        failureLog.setStartTime(startTime);
        failureLog.setEndTime(LocalDateTime.now());
        failureLog.setJobCount(jobCount);
        failureLog.setErrorMessage(truncateErrorMessage(errorMessage));
        xmlFeedUpdateLogService.save(failureLog);
    }
    
    /**
     * Truncate error message to fit database field length
     * 
     * @param errorMessage original error message
     * @return truncated error message (max 500 characters)
     */
    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        return errorMessage.length() > 500 ? errorMessage.substring(0, 500) : errorMessage;
    }
}
