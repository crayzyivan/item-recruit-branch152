package com.item.service.migration.category;

import java.util.List;
import java.util.Map;

/**
 * Category 转换服务接口
 *
 * 提供 PgCategoryEntity ID 到 JobCategoryEntity ID 的转换功能。
 * 通过 PostgreSQL Category ID 查询名称，再通过名称映射查找对应的 JobCategory ID。
 *
 * @author system
 * @version 1.0
 * @since 2025-10-14
 */
public interface ConvertService {

    /**
     * 根据 PgCategory ID 集合转换为 JobCategory ID 映射
     *
     * 转换逻辑：
     * 1. 根据输入的 PgCategory ID 集合查询对应的 PgCategoryEntity
     * 2. 获取每个 PgCategory 的 name 字段
     * 3. 通过预定义的名称映射表查找对应的 JobCategory ID
     * 4. 返回 PgCategory ID -> JobCategory ID 的映射关系
     *
     * @param pgCategoryIds PostgreSQL Category ID 集合
     * @param nameToJobCategoryIdMapping 名称到 JobCategory ID 的映射表
     * @return Map<Integer, Long> key: PgCategory ID, value: JobCategory ID
     *         只包含能成功映射的记录，无法映射的 ID 不会出现在结果中
     */
    Map<Integer, Integer> convertPgCategoryIdsToJobCategoryIds(List<Integer> pgCategoryIds, Map<String, Integer> nameToJobCategoryIdMapping);
}
