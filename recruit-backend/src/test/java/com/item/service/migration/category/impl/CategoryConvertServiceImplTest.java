package com.item.service.migration.category.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.item.dto.JobCategoryDto;
import com.item.entity.migration.category.PgCategoryEntity;
import com.item.service.JobCategoryService;
import com.item.service.migration.category.ConvertService;
import static com.item.service.migration.category.Constant.CATEGORY_MAP;
import com.item.service.migration.category.PgCategoryService;
import com.item.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Category 转换服务单元测试
 *
 * 测试 CategoryConvertService 的 ID 转换功能。
 * 使用 Mock 来模拟 PgCategoryService 的行为。
 *
 * @author system
 * @since 2025-10-14
 */
@Slf4j
@SpringBootTest
class CategoryConvertServiceImplTest {

    @Autowired
    private ConvertService categoryConvertService;
    @Autowired
    private JobCategoryService jobCategoryService;
    @Autowired
    private PgCategoryService pgCategoryService;

    private Map<String, Integer> nameToJobCategoryIdMapping;
    private Map<Integer, String> idToJobCategoryNameMapping;
    private List<PgCategoryEntity> mockPgCategories;
    private Map<Integer, String> mockPgCategoriesMap;
    private List<Integer> ids;

    @BeforeEach
    void setUp() {
        List<JobCategoryDto> allJobCategories = jobCategoryService.getAllJobCategories();
        nameToJobCategoryIdMapping = allJobCategories.stream().collect(Collectors.toMap(JobCategoryDto::getName, JobCategoryDto::getId, (v1, v2) -> {
            log.error("Duplicate key {}", v1);
            return v1;
        }));
        idToJobCategoryNameMapping = allJobCategories.stream().collect(Collectors.toMap(JobCategoryDto::getId, JobCategoryDto::getName, (v1, v2) -> {
            log.error("Duplicate key {}", v1);
            return v1;
        }));
        ids = IntStream.range(1, 22).boxed().collect(Collectors.toList());
        mockPgCategories = pgCategoryService.getCategoriesByIds(ids);
        mockPgCategoriesMap = mockPgCategories.stream().collect(Collectors.toMap(PgCategoryEntity::getId, PgCategoryEntity::getName));
    }

    @Test
    void testConvertPgCategoryIdsToJobCategoryIds_success() {
        for (PgCategoryEntity mockPgCategory : mockPgCategories) {
            Integer id = mockPgCategory.getId();
            String name = mockPgCategory.getName();
            name = CATEGORY_MAP.getOrDefault(name, name);
            Integer idMysql = nameToJobCategoryIdMapping.get(name);
            if (idMysql == null) {
                log.warn("not found name {}", name);
            }
        }

        Map<Integer, Integer> integerIntegerMap = categoryConvertService.convertPgCategoryIdsToJobCategoryIds(ids, nameToJobCategoryIdMapping);
        log.info("mapping {}", integerIntegerMap);
        log.info("mapping {}", JsonUtils.toJson(integerIntegerMap));
        integerIntegerMap.forEach((categoryId, jobCategoryId) -> {
            log.info("categoryId {} name {} ", categoryId, mockPgCategoriesMap.get(categoryId));
            log.info("jobCategoryId {} name {} ", jobCategoryId, idToJobCategoryNameMapping.get(jobCategoryId));
        });
    }

}
