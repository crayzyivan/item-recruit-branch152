package com.item.pgmapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.pgentity.PgUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL用户Mapper
 * 
 * @author system
 */
@Mapper
public interface PgUserMapper extends BaseMapper<PgUser> {
}
