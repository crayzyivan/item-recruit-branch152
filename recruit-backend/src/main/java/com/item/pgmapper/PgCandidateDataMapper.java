package com.item.pgmapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.pgentity.PgCandidateData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * PostgreSQL候选人数据Mapper
 * 
 * @author system
 */
@Mapper
public interface PgCandidateDataMapper extends BaseMapper<PgCandidateData> {
    
    /**
     * 分页查询候选人数据
     */
    @Select("SELECT * FROM candidates.candidate_data ORDER BY created_on LIMIT #{limit} OFFSET #{offset}")
    List<PgCandidateData> selectBatch(int offset, int limit);
    
}