package com.item.dto.migration.job;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * Job Migration Response DTO
 * 
 * 用于返回 Job 迁移操作的结果和统计信息。
 *
 * @author system
 * @since 2025-09-30
 */
@Data
@Builder
public class JobMigrationResponseDTO implements Serializable {
    /**
     * 本次迁移处理的总记录数
     */
    private long totalProcessed;

    /**
     * 成功迁移到 MySQL 的记录数
     */
    private long successMigratedToMysql;

    /**
     * 成功同步到 Elasticsearch 的记录数
     */
    private long successSyncedToEs;

    /**
     * 跳过的记录数 (已存在映射关系)
     */
    private long skippedCount;

    /**
     * 失败的记录数
     */
    private long failedCount;

    /**
     * 迁移开始时间
     */
    private String startTime;

    /**
     * 迁移结束时间
     */
    private String endTime;

    /**
     * 迁移耗时 (毫秒)
     */
    private long elapsedTimeMillis;

    /**
     * 迁移状态 (例如: "SUCCESS", "PARTIAL_SUCCESS", "FAILED")
     */
    private String status;

    /**
     * 详细信息或错误消息
     */
    private String message;

    private Set<Integer> totalProcessedIds;
    private Set<Integer> successMigratedToMysqlIds;
    private Set<Integer> skippedCountIds;
    private Set<Integer> failedCountIds;
}
