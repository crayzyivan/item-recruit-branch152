package com.item.pgmapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.ApplicationStageLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL ApplicationStageLog Mapper接口
 *
 * 专门用于操作PostgreSQL数据库中的candidates.application_stage_log表，
 * 继承MyBatis Plus的BaseMapper，提供完整的CRUD操作。
 * 复杂查询通过Service层使用QueryWrapper实现。
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Mapper
public interface PgApplicationStageLogMapper extends BaseMapper<ApplicationStageLogEntity> {

}
