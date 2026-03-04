package com.item.mapper.migration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.migration.DataMigrationMappingEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据迁移映射Mapper接口
 * 
 * 提供PostgreSQL到MySQL数据迁移ID映射关系的数据访问操作，
 * 继承MyBatis Plus的BaseMapper，提供完整的CRUD功能。
 * 复杂查询通过Service层使用QueryWrapper实现。
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
@Mapper
public interface DataMigrationMappingMapper extends BaseMapper<DataMigrationMappingEntity> {
    
    // 继承BaseMapper提供的基础CRUD方法：
    // - insert(T entity): 插入一条记录
    // - deleteById(Serializable id): 根据ID删除
    // - updateById(T entity): 根据ID更新
    // - selectById(Serializable id): 根据ID查询
    // - selectList(Wrapper<T> queryWrapper): 根据条件查询列表
    // - selectOne(Wrapper<T> queryWrapper): 根据条件查询单条记录
    // - selectCount(Wrapper<T> queryWrapper): 根据条件查询总记录数
    // 
    // 复杂查询逻辑通过Service层使用QueryWrapper实现，
    // 无需在此接口中定义自定义SQL方法
}
