package com.item.service.migration.job;

import com.item.dto.migration.job.JobMigrationRequestDTO;
import com.item.dto.migration.job.JobMigrationResponseDTO;
import com.item.dto.migration.job.MigrateJobDTO;
import com.item.service.migration.job.impl.JobMigrationServiceImpl;

import java.util.Set;

/**
 * Job 迁移服务接口
 * 
 * 提供 PostgreSQL Job 数据迁移到 MySQL 和 Elasticsearch 的核心业务功能。
 * 支持批量迁移、单条迁移、进度查询等操作，确保数据一致性和完整性。
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
public interface JobMigrationService {

    /**
     * 批量迁移 Job 数据
     * 
     * 从 PostgreSQL 分页查询 Job 数据，转换后保存到 MySQL 和同步到 ES。
     * 支持断点续传和错误恢复机制。
     * 
     * @param request 迁移请求参数
     * @return 迁移结果统计
     */
    JobMigrationResponseDTO batchMigrateJobs(JobMigrationRequestDTO request);

//    /**
//     * 迁移单个 Job 记录
//     *
//     * 将单个 PostgreSQL Job 记录转换并保存到 MySQL，同时同步到 ES。
//     * 包含完整的数据转换、映射关系保存和错误处理逻辑。
//     *
//     * @param pgJob PostgreSQL Job 实体
//     * @return 迁移成功返回 MySQL 中的 Job ID，失败返回 null
//     */
//    Long migrateSingleJob(PgJobEntity pgJob);
//
//    /**
//     * 强制重新迁移单个 Job 记录
//     *
//     * 忽略已存在的映射关系，强制重新迁移指定的 Job 记录。
//     * 用于数据修复和重新同步场景。
//     *
//     * @param pgJob PostgreSQL Job 实体
//     * @return 迁移成功返回 MySQL 中的 Job ID，失败返回 null
//     */
//    Long forceMigrateSingleJob(PgJobEntity pgJob);

    /**
     * 检查 Job 是否已迁移
     *
     * 根据 PostgreSQL Job ID 检查是否已存在映射关系。
     *
     * @param pgJobId PostgreSQL Job ID
     * @return 已迁移返回 true，未迁移返回 false
     */
    boolean isJobMigrated(Integer pgJobId);

    JobMigrationServiceImpl.BatchProcessResult processSinglePageJobs(int pageNum, int batchSize, JobMigrationRequestDTO request,
                                                                             Set<Integer> totalProcessedIds, Set<Integer> successMigratedToMysqlIds,
                                                                             Set<Integer> skippedCountIds, Set<Integer> failedCountIds);
    Long doMigrateSingleJob(MigrateJobDTO migrateJobDTO);
//
//    /**
//     * 获取迁移统计信息
//     *
//     * 统计当前 Job 迁移的总体进度和状态。
//     *
//     * @return 迁移统计信息
//     */
//    JobMigrationResponseDTO getMigrationStatistics();

    /**
     * 迁移 Ayrshare 公司配置数据
     * 
     * 从 PostgreSQL companies.company_data.ayrshare_profile_key 迁移数据到 
     * MySQL r_ayrshare_company_config 表。根据 r_data_migration_pgsql_mysql_id 
     * 表中 bustype=8 的租户信息解析 companyCode，检查是否已存在配置，
     * 不存在则执行插入操作。
     * 
     * @return 迁移结果统计
     */
    JobMigrationResponseDTO migrateAyrshareCompanyConfig();
//
//    /**
//     * 验证迁移数据的完整性
//     *
//     * 对比 PostgreSQL 和 MySQL 中的数据，验证迁移的正确性。
//     *
//     * @param pgJobId PostgreSQL Job ID
//     * @return 验证通过返回 true，验证失败返回 false
//     */
//    boolean validateMigratedJob(Integer pgJobId);
//
//    /**
//     * 清理失败的迁移记录
//     *
//     * 清理迁移过程中产生的不完整或错误数据。
//     *
//     * @return 清理的记录数
//     */
//    int cleanupFailedMigrations();
}
