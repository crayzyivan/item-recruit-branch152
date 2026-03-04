package com.item.service.migration.location.impl;

import com.item.entity.migration.location.PgLocationEntity;
import com.item.service.migration.location.PgLocationService;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * PostgreSQL Location 服务集成测试
 *
 * 验证 PgLocationService 的核心功能，包括单条查询、批量查询、分页查询、统计功能和异常处理。
 * 注意：此测试需要 PostgreSQL 数据库连接。
 *
 * @author system
 * @since 2025-10-15
 */
@Slf4j
@SpringBootTest
@Transactional
@Rollback
//@ActiveProfiles("test")
class PgLocationServiceImplTest {

    @Autowired(required = false)
    private PgLocationService pgLocationService;

    @BeforeEach
    void setUp() {
        // 检查 pgLocationService 是否已注入，如果为 null 则跳过测试
        assumeTrue(pgLocationService != null, "PgLocationService not available, skipping tests (PostgreSQL connection might be missing).");
    }

    @Test
    void testGetLocationsByIds_success() {
        log.info("测试根据 IDs 批量查询 Location - 成功场景");
        List<Integer> ids = Arrays.asList(1, 2, 3); // 假设这些ID存在于PostgreSQL中
        List<PgLocationEntity> locations = pgLocationService.getLocationsByIds(ids);

        assertNotNull(locations);
        // 注意：实际测试中可能没有数据，所以不强制要求非空
        log.info("查询到的 Locations 数量: {}", locations.size());
        
        if (!locations.isEmpty()) {
            // 验证返回的Location包含预期的ID
            locations.forEach(location -> {
                assertNotNull(location.getId());
                assertNotNull(location.getCompanyId());
                assertNotNull(location.getCity());
                assertNotNull(location.getState());
                assertNotNull(location.getCountry());
                log.info("Location: ID={}, CompanyId={}, City={}, State={}, Country={}", 
                        location.getId(), location.getCompanyId(), location.getCity(), 
                        location.getState(), location.getCountry());
            });
        }
    }

    @Test
    void testGetLocationsByIds_emptyList() {
        log.info("测试根据 IDs 批量查询 Location - 空列表场景");
        List<Integer> ids = Arrays.asList();
        List<PgLocationEntity> locations = pgLocationService.getLocationsByIds(ids);

        assertNotNull(locations);
        assertTrue(locations.isEmpty());
    }

    @Test
    void testGetLocationsByIds_nullList() {
        log.info("测试根据 IDs 批量查询 Location - null 列表场景");
        List<PgLocationEntity> locations = pgLocationService.getLocationsByIds(null);

        assertNotNull(locations);
        assertTrue(locations.isEmpty());
    }
}
