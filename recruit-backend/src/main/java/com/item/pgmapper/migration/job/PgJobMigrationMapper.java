package com.item.pgmapper.migration.job;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.migration.job.PgJobEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL Job 迁移 Mapper 接口
 * 
 * 专门用于操作 PostgreSQL 数据库中的 companies.jobs 表，
 * 继承 MyBatis Plus 的 BaseMapper，提供完整的 CRUD 操作。
 * 复杂查询通过 Service 层使用 QueryWrapper 实现。
 * 
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
@Mapper
public interface PgJobMigrationMapper extends BaseMapper<PgJobEntity> {

}
