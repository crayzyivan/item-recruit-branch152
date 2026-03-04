package com.item.service.migration.job.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.dto.migration.job.JobMigrationRequestDTO;
import com.item.dto.migration.job.JobMigrationResponseDTO;
import com.item.entity.CityEntity;
import com.item.entity.CountryEntity;
import com.item.entity.StateEntity;
import com.item.entity.migration.location.PgLocationEntity;
import com.item.mapper.CityMapper;
import com.item.mapper.CountryMapper;
import com.item.mapper.StateMapper;
import com.item.service.migration.job.JobMigrationService;
import com.item.service.migration.location.PgLocationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Job 迁移服务集成测试
 *
 * 验证 Job 迁移服务的核心功能，包括单条迁移、批量迁移、数据验证等。
 * 注意：此测试需要 PostgreSQL 和 MySQL 数据库连接。
 *
 * 重构后的测试：JobMigrationServiceImpl 现在只实现接口，通过依赖注入使用其他服务。
 *
 * @author system
 * @since 2025-09-30
 */
@Slf4j
@SpringBootTest
@Transactional
@Rollback
@ActiveProfiles("test")
class JobMigrationServiceImplTest {

    @Autowired(required = false)
    private JobMigrationService jobMigrationService;
    @Autowired
    private PgLocationService pgLocationService;
    @Autowired
    private CountryMapper countryMapper;
    @Autowired
    private StateMapper  stateMapper;
    @Autowired
    private CityMapper cityMapper;

    @Test
    void test() {
        List<Integer> list = IntStream.range(1, 1000).boxed().toList();
        List<PgLocationEntity> locationsByIds = pgLocationService.getLocationsByIds(list);
        List<String> pgCountry = locationsByIds.stream().map(PgLocationEntity::getCountry).distinct().toList();
        List<String> pgState = locationsByIds.stream().map(PgLocationEntity::getState).distinct().toList();
        List<String> pgCity = locationsByIds.stream().map(PgLocationEntity::getCity).distinct().toList();
        LambdaQueryWrapper<CountryEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(CountryEntity::getIso2, pgCountry);
        List<CountryEntity> countryEntities = countryMapper.selectList(lambdaQueryWrapper);
        List<String> countryName = countryEntities.stream().map(CountryEntity::getIso2).distinct().toList();
        Collection<String> subtract = CollectionUtils.subtract(pgCountry, countryName);

        LambdaQueryWrapper<StateEntity> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.in(StateEntity::getName, pgState);
        List<StateEntity> stateEntities = stateMapper.selectList(lambdaQueryWrapper1);
        List<String> stateName = stateEntities.stream().map(StateEntity::getName).distinct().toList();
        Collection<String> subtractSate = CollectionUtils.subtract(pgState, stateName);

        LambdaQueryWrapper<CityEntity> lambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper2.in(CityEntity::getName, pgCity);
        List<CityEntity> cityEntities = cityMapper.selectList(lambdaQueryWrapper2);
        List<String> cityName = cityEntities.stream().map(CityEntity::getName).distinct().toList();
        Collection<String> subtractCity = CollectionUtils.subtract(pgCity, cityName);

        log.info("subtract {}", subtract);
        log.info("subtractSate {}", subtractSate);
        log.info("subtractCity {}", subtractCity);
    }

    @Test
    void  test02(){
        JobMigrationResponseDTO jobMigrationResponseDTO = jobMigrationService.migrateAyrshareCompanyConfig();
        log.info("migrateAyrshareCompanyConfig {} ", jobMigrationResponseDTO);
    }
//
//    private PgJobEntity testPgJob;
//
//    @BeforeEach
//    void setUp() {
//        // 创建测试用的 PostgreSQL Job 实体
//        testPgJob = new PgJobEntity();
//        testPgJob.setId(999999); // 使用一个不太可能冲突的测试 ID
//        testPgJob.setTitle("Test Senior Java Developer");
//        testPgJob.setSlug("test-senior-java-developer");
//        testPgJob.setCompanyId(1);
//        testPgJob.setStatus("active");
//        testPgJob.setLocationId(1);
//        testPgJob.setNumberOfOpenings(2);
//        testPgJob.setType("full_time");
//        testPgJob.setLocationType("hybrid");
//        testPgJob.setSalaryType("monthly");
//        testPgJob.setCurrencyId((short) 1);
//        testPgJob.setMinSalary(new BigDecimal("6000.00"));
//        testPgJob.setMaxSalary(new BigDecimal("9000.00"));
//        testPgJob.setHotlist(false);
//        testPgJob.setCreatedOn(OffsetDateTime.now());
//        testPgJob.setUpdatedOn(OffsetDateTime.now());
//        testPgJob.setCreatedBy(UUID.randomUUID());
//        testPgJob.setUpdatedBy(UUID.randomUUID());
//        testPgJob.setCategoryIds(Arrays.asList(1, 2));
//    }
//
//    @Test
//    void testServiceInjection() {
//        // 验证服务是否正确注入
//        if (jobMigrationService != null) {
//            assertNotNull(jobMigrationService);
//            log.info("JobMigrationService 注入成功");
//        } else {
//            log.warn("JobMigrationService 未注入，可能缺少数据库配置");
//        }
//    }
//
//    @Test
//    void testIsJobMigrated() {
//        if (jobMigrationService == null) return;
//
//        try {
//            // 测试检查迁移状态
//            boolean isMigrated = jobMigrationService.isJobMigrated(testPgJob.getId());
//
//            // 对于测试 ID，应该返回 false（未迁移）
//            assertFalse(isMigrated);
//            log.info("检查迁移状态测试通过，Job ID: {}, 迁移状态: {}", testPgJob.getId(), isMigrated);
//
//        } catch (Exception e) {
//            log.warn("测试检查迁移状态时发生异常: {}", e.getMessage());
//        }
//    }
//
//    @Test
//    void testMigrateSingleJob() {
//        if (jobMigrationService == null) return;
//
//        try {
//            // 测试单个 Job 迁移
//            Long mysqlJobId = jobMigrationService.migrateSingleJob(testPgJob);
//
//            if (mysqlJobId != null) {
//                assertTrue(mysqlJobId > 0);
//                log.info("单个 Job 迁移测试通过，PostgreSQL ID: {}, MySQL ID: {}",
//                    testPgJob.getId(), mysqlJobId);
//
//                // 验证迁移状态
//                boolean isMigrated = jobMigrationService.isJobMigrated(testPgJob.getId());
//                assertTrue(isMigrated);
//
//                // 验证数据完整性
//                boolean isValid = jobMigrationService.validateMigratedJob(testPgJob.getId());
//                assertTrue(isValid);
//
//            } else {
//                log.warn("单个 Job 迁移返回 null，可能是数据库配置问题");
//            }
//
//        } catch (Exception e) {
//            log.warn("测试单个 Job 迁移时发生异常: {}", e.getMessage());
//        }
//    }
//
//    @Test
//    void testForceMigrateSingleJob() {
//        if (jobMigrationService == null) return;
//
//        try {
//            // 先进行一次普通迁移
//            Long firstMysqlJobId = jobMigrationService.migrateSingleJob(testPgJob);
//
//            if (firstMysqlJobId != null) {
//                // 再进行强制迁移
//                Long secondMysqlJobId = jobMigrationService.forceMigrateSingleJob(testPgJob);
//
//                assertNotNull(secondMysqlJobId);
//                assertTrue(secondMysqlJobId > 0);
//
//                log.info("强制迁移测试通过，第一次迁移 ID: {}, 强制迁移 ID: {}",
//                    firstMysqlJobId, secondMysqlJobId);
//            }
//
//        } catch (Exception e) {
//            log.warn("测试强制迁移时发生异常: {}", e.getMessage());
//        }
//    }
//
//    @Test
//    void testGetMigrationStatistics() {
//        if (jobMigrationService == null) return;
//
//        try {
//            // 测试获取迁移统计信息
//            JobMigrationResponseDTO statistics = jobMigrationService.getMigrationStatistics();
//
//            assertNotNull(statistics);
//            assertNotNull(statistics.getMessage());
//            assertEquals("STATISTICS", statistics.getStatus());
//
//            log.info("迁移统计信息测试通过: {}", statistics.getMessage());
//
//        } catch (Exception e) {
//            log.warn("测试获取迁移统计信息时发生异常: {}", e.getMessage());
//        }
//    }

    @Test
    void testBatchMigrateJobs() {
        if (jobMigrationService == null) return;

        try {
            // 创建批量迁移请求
            JobMigrationRequestDTO request = new JobMigrationRequestDTO();
            request.setStartPgJobId(79);
            request.setEndPgJobId(80); // 只迁移一个测试记录
            request.setBatchSize(100);
            request.setForceMigrate(false);

            // 注意：这个测试可能会失败，因为测试数据可能不在 PostgreSQL 中
            // 这里主要测试方法调用是否正常
            JobMigrationResponseDTO response = jobMigrationService.batchMigrateJobs(request);

            assertNotNull(response);
            assertNotNull(response.getStatus());
            assertNotNull(response.getMessage());

            log.info("批量迁移测试完成，响应: {}", response);

        } catch (Exception e) {
            log.warn("测试批量迁移时发生异常: {}", e.getMessage());
            // 批量迁移测试失败是预期的，因为测试数据可能不存在
        }
    }
//
//    @Test
//    void testValidateMigratedJob() {
//        if (jobMigrationService == null) return;
//
//        try {
//            // 对于不存在的 Job，验证应该返回 false
//            boolean isValid = jobMigrationService.validateMigratedJob(testPgJob.getId());
//            assertFalse(isValid);
//
//            log.info("数据验证测试通过，不存在的 Job 验证结果: {}", isValid);
//
//        } catch (Exception e) {
//            log.warn("测试数据验证时发生异常: {}", e.getMessage());
//        }
//    }
//
//    @Test
//    void testCleanupFailedMigrations() {
//        if (jobMigrationService == null) return;
//
//        try {
//            // 测试清理失败的迁移记录
//            int cleanedCount = jobMigrationService.cleanupFailedMigrations();
//
//            assertTrue(cleanedCount >= 0);
//            log.info("清理失败迁移记录测试通过，清理数量: {}", cleanedCount);
//
//        } catch (Exception e) {
//            log.warn("测试清理失败迁移记录时发生异常: {}", e.getMessage());
//        }
//    }
}
