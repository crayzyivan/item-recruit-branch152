package com.item.controller.migration.job;

import com.item.convert.migration.job.JobMigrationConvert;
import com.item.dto.migration.job.DataMigrationMappingDTO;
import com.item.dto.migration.job.JobMigrationRequestDTO;
import com.item.dto.migration.job.JobMigrationResponseDTO;
import com.item.dto.migration.job.MappingDataDTO;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.framework.config.RecruitCommonNacosConfig;
import com.item.service.migration.DataMigrationMappingService;
import com.item.service.migration.job.JobMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Job 迁移控制器
 * 
 * 提供 PostgreSQL Job 数据迁移到 MySQL 和 Elasticsearch 的 RESTful API 接口。
 * 支持批量迁移、单条迁移、进度查询、统计信息获取等功能。
 * 
 * API 路径设计：
 * - POST /api/v1/migration/job/migrate/batch - 批量迁移
 * - POST /api/v1/migration/job/migrate/single/{id} - 单条迁移
 * - POST /api/v1/migration/job/migrate/force/{id} - 强制重新迁移
 * - GET /api/v1/migration/job/progress - 查询迁移进度
 * - GET /api/v1/migration/job/statistics - 获取迁移统计
 * - POST /api/v1/migration/job/validate/{id} - 验证迁移数据
 * - DELETE /api/v1/migration/job/cleanup - 清理失败的迁移记录
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@RestController
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequestMapping("/api/v1/migration")
@RequiredArgsConstructor
public class JobMigrationController {

    private final JobMigrationService jobMigrationService;
    private final DataMigrationMappingService dataMigrationMappingService;
    private final JobMigrationConvert jobMigrationConvert;
    private final RedissonClient redissonClient;
    private final RecruitCommonNacosConfig commonNacosConfig;

    // 响应状态常量
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String ACCESS_DENIED = "ACCESS_DENIED";


    /**
     * 批量迁移 Job 数据
     * 
     * 从 PostgreSQL 分页查询 Job 数据，转换后保存到 MySQL 和同步到 ES。
     * 支持断点续传、ID 范围过滤、强制重新迁移等功能。
     * 
     * @param request 迁移请求参数
     * @return 迁移结果统计
     */
    @PostMapping("/job/migrate/batch")
    public JobMigrationResponseDTO batchMigrateJobs(@RequestBody @Validated JobMigrationRequestDTO request) {
        log.info("开始批量迁移 Job 数据，请求参数: {}", request);
        if (!isAccess(request.getSecurity())) {
            return JobMigrationResponseDTO.builder().status(STATUS_FAILED).message(ACCESS_DENIED).build();
        }
        try {
            JobMigrationResponseDTO response = jobMigrationService.batchMigrateJobs(request);
            log.info("批量迁移完成，结果: {}", response);
            return response;
        } catch (Exception e) {
            log.error("批量迁移 Job 数据失败", e);
            return JobMigrationResponseDTO.builder()
                .status(STATUS_FAILED)
                .message("批量迁移失败: " + e.getMessage())
                .totalProcessed(0L)
                .successMigratedToMysql(0L)
                .successSyncedToEs(0L)
                .skippedCount(0L)
                .failedCount(0L)
                .build();
        }
    }

    @PostMapping("/migrate/batch/mapping")
    public Integer batchMigrateMapping(@RequestBody @Validated MappingDataDTO request) {
        log.info("开始批量插入映射数据 请求参数: {}", request);
        if (!isAccess(request.getSecurity())) {
            log.warn("batchMigrateMapping {}", ACCESS_DENIED);
            return 0;
        }
        try {
            List<DataMigrationMappingDTO> mappingData = request.getMappingData();
            List<DataMigrationMappingEntity> dataMigrationMappingEntities = jobMigrationConvert.convert2DataMigrationMappingEntitys(mappingData);
            int saveBatchMappings = dataMigrationMappingService.saveBatchMappings(dataMigrationMappingEntities);
            log.info("批量插入完成，结果: {}", saveBatchMappings);
            return saveBatchMappings;
        } catch (Exception e) {
            log.error("批量迁移 Job 数据失败", e);
            return 0;
        }
    }

    @DeleteMapping("/del/redis/category/cache")
    public void delRedisKey(@RequestParam(name = "security", required = false) String security, @RequestParam(name = "cacheKey", required = false) String cacheKey) {
        if (!isAccess(security)) {
            log.warn("delRedisKey {}", ACCESS_DENIED);
            return;
        }
        if (StringUtils.isBlank(cacheKey)) {
            log.warn("delRedisKey {} {}", ACCESS_DENIED, cacheKey);
            return;
        }
        log.info("delRedisKey {}", cacheKey);
        redissonClient.getBucket(cacheKey).delete();
    }
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
    @PostMapping("/ayrshare/migrate")
    public JobMigrationResponseDTO migrateAyrshareConfig(@RequestParam(name = "security", required = false) String security) {
        log.info("开始迁移 Ayrshare 公司配置数据");
        if (!isAccess(security)) {
            log.warn("migrateAyrshareConfig {}", ACCESS_DENIED);
            return JobMigrationResponseDTO.builder().status(STATUS_FAILED).message(ACCESS_DENIED).build();
        }
        try {
            JobMigrationResponseDTO response = jobMigrationService.migrateAyrshareCompanyConfig();
            log.info("Ayrshare 配置迁移完成，结果: {}", response.getMessage());
            return response;
        } catch (Exception e) {
            log.error("迁移 Ayrshare 配置数据失败", e);
            return JobMigrationResponseDTO.builder()
                .status(STATUS_FAILED)
                .message("Ayrshare 配置迁移失败: " + e.getMessage())
                .totalProcessed(0L)
                .successMigratedToMysql(0L)
                .successSyncedToEs(0L)
                .skippedCount(0L)
                .failedCount(0L)
                .build();
        }
    }

    private boolean isAccess(String security) {
        String rdControllerSecurity = commonNacosConfig.getRdControllerSecurity();
        if (StringUtils.isBlank(security) || StringUtils.isNotBlank(security) && !Strings.CS.equals(rdControllerSecurity, security)) {
            log.warn("RDInnerController updateJobLocation rdControllerSecurity {} security {}", rdControllerSecurity, security);
            return false;
        }
        return true;
    }
}
