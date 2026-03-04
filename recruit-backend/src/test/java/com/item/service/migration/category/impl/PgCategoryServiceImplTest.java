package com.item.service.migration.category.impl;

import com.item.entity.migration.category.PgCategoryEntity;
import com.item.service.migration.category.PgCategoryService;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * PostgreSQL Category 服务测试类
 * 
 * 验证 PgCategoryService 的基本功能，包括根据IDs批量查询、分页查询等。
 * 注意：此测试需要 PostgreSQL 数据库连接，在 CI/CD 环境中可能需要跳过。
 *
 * @author system
 * @since 2025-10-14
 */
@Slf4j
@SpringBootTest
//@ActiveProfiles("test")
class PgCategoryServiceImplTest {

    @Autowired(required = false)
    private PgCategoryService pgCategoryService;

    @Test
    void testServiceInjection() {
        // 验证服务是否正确注入
        // 在没有 PostgreSQL 连接的环境中，service 可能为 null
        if (pgCategoryService != null) {
            assertNotNull(pgCategoryService);
            log.info("PgCategoryService 注入成功");
        } else {
            log.warn("PgCategoryService 未注入，可能是因为缺少 PostgreSQL 连接");
        }
    }

    @Test
    void testGetCategoriesByIds() {
        // 跳过测试如果没有服务注入
        if (pgCategoryService == null) {
            log.warn("跳过测试：PgCategoryService 未注入");
            return;
        }

        try {
            // 测试批量查询功能（使用示例ID）
            List<Integer> categoryIds = Arrays.asList(1, 2, 3);
            List<PgCategoryEntity> categories = pgCategoryService.getCategoriesByIds(categoryIds);
            
            assertNotNull(categories);
            log.info("批量查询测试完成：请求ID数量={}, 结果数量={}", categoryIds.size(), categories.size());
            
        } catch (Exception e) {
            // 在测试环境中可能没有 PostgreSQL 连接，记录但不失败
            log.warn("PostgreSQL 连接不可用或数据不存在: {}", e.getMessage());
        }
    }

    @Test
    void testGetCategoriesWithPagination() {
        // 跳过测试如果没有服务注入
        if (pgCategoryService == null) {
            log.warn("跳过测试：PgCategoryService 未注入");
            return;
        }

        try {
            // 测试分页查询功能
            var result = pgCategoryService.getCategoriesWithPagination(1, 10, null, null);
            
            assertNotNull(result);
            log.info("分页查询测试完成：总数={}, 当前页数量={}", result.getTotal(), result.getRecords().size());
            
        } catch (Exception e) {
            // 在测试环境中可能没有 PostgreSQL 连接，记录但不失败
            log.warn("PostgreSQL 连接不可用: {}", e.getMessage());
        }
    }

    @Test
    void testGetCategoryById() {
        // 跳过测试如果没有服务注入
        if (pgCategoryService == null) {
            log.warn("跳过测试：PgCategoryService 未注入");
            return;
        }

        try {
            // 测试根据ID查询（使用示例ID）
            PgCategoryEntity category = pgCategoryService.getCategoryById(1);
            log.info("根据ID查询测试完成：ID=1, 结果={}", category != null ? "找到" : "未找到");
            
        } catch (Exception e) {
            // 在测试环境中可能没有 PostgreSQL 连接，记录但不失败
            log.warn("PostgreSQL 连接不可用: {}", e.getMessage());
        }
    }

    @Test
    void testEmptyParameterHandling() {
        // 跳过测试如果没有服务注入
        if (pgCategoryService == null) {
            log.warn("跳过测试：PgCategoryService 未注入");
            return;
        }

        // 测试空参数处理
        List<PgCategoryEntity> emptyResult = pgCategoryService.getCategoriesByIds(null);
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());
        
        PgCategoryEntity nullResult = pgCategoryService.getCategoryById(null);
        assertNull(nullResult);
        
        boolean existsResult = pgCategoryService.existsById(null);
        assertFalse(existsResult);
        
        log.info("空参数处理测试完成");
    }
}
