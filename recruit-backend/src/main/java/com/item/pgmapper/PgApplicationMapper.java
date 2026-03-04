package com.item.pgmapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.pgentity.PgApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL申请表Mapper
 * 
 * @author system
 */
@Mapper
public interface PgApplicationMapper extends BaseMapper<PgApplication> {
    
}
