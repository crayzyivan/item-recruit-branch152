package com.item.pgmapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.pgentity.PgEmploymentHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL工作历史Mapper
 * 
 * @author system
 */
@Mapper
public interface PgEmploymentHistoryMapper extends BaseMapper<PgEmploymentHistory> {
    
}
