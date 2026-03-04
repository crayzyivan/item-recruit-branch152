package com.item.service.migration.category.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.migration.category.PgCategoryEntity;
import com.item.pgmapper.migration.category.PgCategoryMigrationMapper;
import com.item.service.migration.category.PgCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL Category 服务实现类
 * 
 * 实现对 PostgreSQL 数据库中 common.categories 表的业务操作。
 * 继承 ServiceImpl 以便直接使用 MyBatis Plus 的 CRUD 方法。
 * 使用 LambdaQueryWrapper 实现类型安全的复杂查询逻辑。
 *
 * @author system
 * @version 1.0
 * @since 2025-10-14
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
public class PgCategoryServiceImpl extends ServiceImpl<PgCategoryMigrationMapper, PgCategoryEntity> implements PgCategoryService {

    @Override
    public List<PgCategoryEntity> getCategoriesByIds(List<Integer> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            log.warn("getCategoriesByIds failed: categoryIds list is null or empty");
            return new ArrayList<>();
        }

        log.debug("根据IDs批量查询 PostgreSQL Category 数据: categoryIds={}", categoryIds);

        try {
            // 构建查询条件
            LambdaQueryWrapper<PgCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(PgCategoryEntity::getId, categoryIds)
                       .orderByAsc(PgCategoryEntity::getId);

            List<PgCategoryEntity> result = this.list(queryWrapper);
            log.debug("批量查询完成: 请求ID数量={}, 查询结果数量={}", categoryIds.size(), result.size());
            return result;
        } catch (Exception e) {
            log.error("根据IDs批量查询 PostgreSQL Category 数据失败: categoryIds={}", categoryIds, e);
            return new ArrayList<>();
        }
    }

    @Override
    public IPage<PgCategoryEntity> getCategoriesWithPagination(int pageNum, int pageSize, Integer startCategoryId, Integer endCategoryId) {
        log.debug("分页查询 PostgreSQL Category 数据: pageNum={}, pageSize={}, startCategoryId={}, endCategoryId={}", 
                pageNum, pageSize, startCategoryId, endCategoryId);

        try {
            // 构建查询条件
            LambdaQueryWrapper<PgCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(PgCategoryEntity::getId);

            // 添加 ID 范围条件
            if (startCategoryId != null && endCategoryId != null) {
                queryWrapper.between(PgCategoryEntity::getId, startCategoryId, endCategoryId);
            } else if (startCategoryId != null) {
                queryWrapper.ge(PgCategoryEntity::getId, startCategoryId);
            } else if (endCategoryId != null) {
                queryWrapper.le(PgCategoryEntity::getId, endCategoryId);
            }

            // 执行分页查询
            IPage<PgCategoryEntity> result = this.page(new Page<>(pageNum, pageSize), queryWrapper);
            
            log.debug("分页查询完成: 总记录数={}, 当前页记录数={}", result.getTotal(), result.getRecords().size());
            return result;
        } catch (Exception e) {
            log.error("分页查询 PostgreSQL Category 数据失败: pageNum={}, pageSize={}, startCategoryId={}, endCategoryId={}", 
                    pageNum, pageSize, startCategoryId, endCategoryId, e);
            // 返回空的分页结果
            return new Page<>(pageNum, pageSize);
        }
    }

    @Override
    public long getTotalCategoryCount(Integer startCategoryId, Integer endCategoryId) {
        log.debug("统计 PostgreSQL Category 总数: startCategoryId={}, endCategoryId={}", startCategoryId, endCategoryId);

        try {
            // 构建查询条件
            LambdaQueryWrapper<PgCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();

            // 添加 ID 范围条件
            if (startCategoryId != null && endCategoryId != null) {
                queryWrapper.between(PgCategoryEntity::getId, startCategoryId, endCategoryId);
            } else if (startCategoryId != null) {
                queryWrapper.ge(PgCategoryEntity::getId, startCategoryId);
            } else if (endCategoryId != null) {
                queryWrapper.le(PgCategoryEntity::getId, endCategoryId);
            }

            long count = this.count(queryWrapper);
            log.debug("统计完成: 总记录数={}", count);
            return count;
        } catch (Exception e) {
            log.error("统计 PostgreSQL Category 总数失败: startCategoryId={}, endCategoryId={}", startCategoryId, endCategoryId, e);
            return 0L;
        }
    }

    @Override
    public PgCategoryEntity getCategoryById(Integer categoryId) {
        if (categoryId == null) {
            log.warn("getCategoryById failed: categoryId is null");
            return null;
        }

        try {
            PgCategoryEntity category = this.getById(categoryId);
            log.debug("根据 ID 查询 Category: categoryId={}, found={}", categoryId, category != null);
            return category;
        } catch (Exception e) {
            log.error("根据 ID 查询 Category 失败: categoryId={}", categoryId, e);
            return null;
        }
    }

    @Override
    public boolean existsById(Integer categoryId) {
        if (categoryId == null) {
            return false;
        }

        try {
            LambdaQueryWrapper<PgCategoryEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PgCategoryEntity::getId, categoryId);
            
            long count = this.count(wrapper);
            boolean exists = count > 0;
            log.debug("检查 Category 是否存在: categoryId={}, exists={}", categoryId, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查 Category 是否存在失败: categoryId={}", categoryId, e);
            return false;
        }
    }

    @Override
    public PgCategoryEntity getCategoryByName(String name) {
        if (StringUtils.isBlank(name)) {
            log.warn("getCategoryByName failed: name is blank");
            return null;
        }

        try {
            LambdaQueryWrapper<PgCategoryEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PgCategoryEntity::getName, name);
            
            PgCategoryEntity category = this.getOne(wrapper);
            log.debug("根据名称查询 Category: name={}, found={}", name, category != null);
            return category;
        } catch (Exception e) {
            log.error("根据名称查询 Category 失败: name={}", name, e);
            return null;
        }
    }

    @Override
    public List<PgCategoryEntity> getCategoriesByNamePattern(String namePattern) {
        if (StringUtils.isBlank(namePattern)) {
            log.warn("getCategoriesByNamePattern failed: namePattern is blank");
            return new ArrayList<>();
        }

        try {
            LambdaQueryWrapper<PgCategoryEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(PgCategoryEntity::getName, namePattern)
                   .orderByAsc(PgCategoryEntity::getId);
            
            List<PgCategoryEntity> categories = this.list(wrapper);
            log.debug("根据名称模式查询 Category: namePattern={}, count={}", namePattern, categories.size());
            return categories;
        } catch (Exception e) {
            log.error("根据名称模式查询 Category 失败: namePattern={}", namePattern, e);
            return new ArrayList<>();
        }
    }
}
