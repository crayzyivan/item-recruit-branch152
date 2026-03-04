package com.item.service.migration.category;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.migration.category.PgCategoryEntity;

import java.util.List;

/**
 * PostgreSQL Category 服务接口
 * 
 * 提供对 PostgreSQL 数据库中 common.categories 表的业务操作。
 * 继承 MyBatis Plus 的 IService 接口，提供完整的 CRUD 操作。
 * 支持根据主键IDs批量查询等业务场景。
 *
 * @author system
 * @version 1.0
 * @since 2025-10-14
 */
public interface PgCategoryService extends IService<PgCategoryEntity> {

    /**
     * 根据主键IDs批量查询分类数据
     * 
     * 支持根据多个分类ID查询对应的分类信息，用于数据迁移和关联查询场景。
     * 
     * @param categoryIds 分类ID列表
     * @return 分类实体列表，如果没有找到返回空列表
     */
    List<PgCategoryEntity> getCategoriesByIds(List<Integer> categoryIds);

    /**
     * 分页查询 Category 数据
     * 
     * 支持按 ID 范围过滤的分页查询，用于批量迁移场景。
     * 
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param startCategoryId 起始分类 ID（可选）
     * @param endCategoryId 结束分类 ID（可选）
     * @return 分页结果
     */
    IPage<PgCategoryEntity> getCategoriesWithPagination(int pageNum, int pageSize, Integer startCategoryId, Integer endCategoryId);

    /**
     * 获取 Category 总数
     * 
     * 支持按 ID 范围过滤的总数统计。
     * 
     * @param startCategoryId 起始分类 ID（可选）
     * @param endCategoryId 结束分类 ID（可选）
     * @return 分类总数
     */
    long getTotalCategoryCount(Integer startCategoryId, Integer endCategoryId);

    /**
     * 根据 ID 获取单个 Category
     * 
     * @param categoryId 分类 ID
     * @return 分类实体，不存在返回 null
     */
    PgCategoryEntity getCategoryById(Integer categoryId);

    /**
     * 检查 Category 是否存在
     * 
     * @param categoryId 分类 ID
     * @return 存在返回 true，不存在返回 false
     */
    boolean existsById(Integer categoryId);

    /**
     * 根据名称查询分类
     * 
     * @param name 分类名称
     * @return 分类实体，不存在返回 null
     */
    PgCategoryEntity getCategoryByName(String name);

    /**
     * 根据名称模糊查询分类列表
     * 
     * @param namePattern 名称模式（支持模糊匹配）
     * @return 分类实体列表
     */
    List<PgCategoryEntity> getCategoriesByNamePattern(String namePattern);
}
