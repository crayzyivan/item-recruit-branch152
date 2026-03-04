package com.item.pgmapper.migration.location;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.migration.location.PgLocationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * PostgreSQL Location 迁移 Mapper 接口
 *
 * 提供对 PostgreSQL 数据库中 companies.locations 表的数据访问操作。
 * 继承 BaseMapper 以便直接使用 MyBatis Plus 的 CRUD 方法。
 *
 * @author system
 * @since 2025-10-15
 */
@Mapper
public interface PgLocationMigrationMapper extends BaseMapper<PgLocationEntity> {
}
