package com.item.pgmapper.migration.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.migration.company.PgCompanyEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL Company 数据 Mapper 接口
 * 
 * 专门用于操作 PostgreSQL 数据库中的 companies.company_data 表，
 * 继承 MyBatis Plus 的 BaseMapper，提供完整的 CRUD 操作。
 * 主要用于查询公司的 ayrshare_profile_key 配置信息。
 * 
 * @author system
 * @version 1.0
 * @since 2025-10-15
 */
@Mapper
public interface PgCompanyMapper extends BaseMapper<PgCompanyEntity> {

}

