package com.item.pgmapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.pgentity.PgResumeData;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL简历数据Mapper
 * 
 * @author system
 */
@Mapper
public interface PgResumeDataMapper extends BaseMapper<PgResumeData> {
    
}
