package com.item.pgmapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.ApplicationInterviewReportsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL面试报告Mapper接口
 * 
 * 专门用于操作PostgreSQL数据库中的candidates.application_interview_reports表，
 * 继承MyBatis Plus的BaseMapper，提供完整的CRUD操作。
 * 复杂查询通过Service层使用QueryWrapper实现。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-28
 */
@Mapper
public interface PgApplicationInterviewReportsMapper extends BaseMapper<ApplicationInterviewReportsEntity> {

}
