package com.item.pgmapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.ApplicationScreeningReportsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL AI筛选报告Mapper接口
 * 
 * 专门用于操作PostgreSQL数据库中的candidates.application_screening_reports表，
 * 继承MyBatis Plus的BaseMapper，提供完整的CRUD操作。
 * 复杂查询通过Service层使用QueryWrapper实现。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Mapper
public interface PgApplicationScreeningReportsMapper extends BaseMapper<ApplicationScreeningReportsEntity> {

}
