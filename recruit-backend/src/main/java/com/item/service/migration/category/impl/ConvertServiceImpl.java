package com.item.service.migration.category.impl;

import com.item.entity.migration.category.PgCategoryEntity;
import com.item.service.migration.category.ConvertService;
import static com.item.service.migration.category.Constant.CATEGORY_MAP;
import com.item.service.migration.category.PgCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Category 转换服务实现类
 *
 * 实现 PgCategoryEntity ID 到 JobCategoryEntity ID 的转换逻辑。
 * 通过 PgCategoryService 查询 PostgreSQL 分类数据，
 * 使用名称映射表进行 ID 转换。
 *
 * @author system
 * @version 1.0
 * @since 2025-10-14
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
public class ConvertServiceImpl implements ConvertService {

    @Autowired
    private PgCategoryService pgCategoryService;

    @Override
    public Map<Integer, Integer> convertPgCategoryIdsToJobCategoryIds(List<Integer> pgCategoryIds, Map<String, Integer> nameToJobCategoryIdMapping) {
        Map<Integer, Integer> result = new HashMap<>();

        if (CollectionUtils.isEmpty(pgCategoryIds)) {
            log.warn("convertPgCategoryIdsToJobCategoryIds: pgCategoryIds list is null or empty");
            return result;
        }

        if (nameToJobCategoryIdMapping == null || nameToJobCategoryIdMapping.isEmpty()) {
            log.warn("convertPgCategoryIdsToJobCategoryIds: nameToJobCategoryIdMapping is null or empty");
            return result;
        }

        try {
            // 1. 根据 ID 集合批量查询 PgCategoryEntity
            List<PgCategoryEntity> pgCategories = pgCategoryService.getCategoriesByIds(pgCategoryIds);
            
            if (CollectionUtils.isEmpty(pgCategories)) {
                log.warn("convertPgCategoryIdsToJobCategoryIds: no PgCategories found for IDs: {}", pgCategoryIds);
                return result;
            }

            int successCount = 0;
            int failCount = 0;

            // 2. 遍历查询结果，进行 ID 转换
            for (PgCategoryEntity pgCategory : pgCategories) {
                if (pgCategory == null) {
                    failCount++;
                    continue;
                }

                Integer pgCategoryId = pgCategory.getId();
                String categoryName = pgCategory.getName();
                log.info("categoryName before {}", categoryName);
                categoryName = CATEGORY_MAP.getOrDefault(categoryName, categoryName);
                log.info("categoryName after {}", categoryName);
                if (pgCategoryId == null) {
                    log.warn("PgCategory has null ID, skipping");
                    failCount++;
                    continue;
                }

                if (StringUtils.isBlank(categoryName)) {
                    log.warn("PgCategory ID {} has null or empty name, skipping", pgCategoryId);
                    failCount++;
                    continue;
                }

                // 3. 通过名称映射查找对应的 JobCategory ID
                Integer jobCategoryId = nameToJobCategoryIdMapping.get(categoryName);
                if (jobCategoryId == null) {
                    log.warn("No JobCategory mapping found for PgCategory ID: {}, Name: '{}'", pgCategoryId, categoryName);
                    failCount++;
                    continue;
                }

                // 4. 添加到结果映射中
                result.put(pgCategoryId, jobCategoryId);
                successCount++;

                log.debug("Successfully mapped PgCategory ID {} ('{}') -> JobCategory ID {}", 
                        pgCategoryId, categoryName, jobCategoryId);
            }

            log.info("convertPgCategoryIdsToJobCategoryIds completed: {} input IDs, {} PgCategories found, {} successful mappings, {} failures", 
                    pgCategoryIds.size(), pgCategories.size(), successCount, failCount);

            return result;

        } catch (Exception e) {
            log.error("convertPgCategoryIdsToJobCategoryIds failed for IDs: {}", pgCategoryIds, e);
            return result;
        }
    }
}
