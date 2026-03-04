package com.item.dto.migration.job;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Job Migration Request DTO
 * 
 * 用于接收 Job 迁移请求参数。
 * 支持指定迁移的起始 ID、结束 ID、批次大小和是否强制重新迁移。
 *
 * @author system
 * @since 2025-09-30
 */
@Data
public class JobMigrationRequestDTO {
    /**
     * 起始 PostgreSQL Job ID (包含)
     */
    private Integer startPgJobId;

    /**
     * 结束 PostgreSQL Job ID (包含)
     */
    private Integer endPgJobId;

    /**
     * 每次处理的批次大小
     */
    @Min(value = 1, message = "Batch size must be at least 100")
    @Max(value = 5000, message = "Batch size cannot exceed 5000")
    private Integer batchSize = 1000;

    /**
     * 是否强制重新迁移已存在的记录 (true: 强制重新迁移, false: 跳过已存在的记录)
     */
    private Boolean forceMigrate = false;

    private String security;
}
