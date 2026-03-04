package com.item.pgmapper.migration.category;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.migration.category.PgCategoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL Category 数据访问接口
 * 
 * 提供对 PostgreSQL common.categories 表的数据访问操作。
 * 继承 MyBatis Plus 的 BaseMapper 接口，提供完整的 CRUD 操作。
 *
 * @author system
 * @version 1.0
 * @since 2025-10-14
 */
@Mapper
public interface PgCategoryMigrationMapper extends BaseMapper<PgCategoryEntity> {
}
