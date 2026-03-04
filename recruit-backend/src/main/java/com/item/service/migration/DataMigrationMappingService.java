package com.item.service.migration;

import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.framework.constant.MigrationBusTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 数据迁移映射服务接口
 * 
 * 提供PostgreSQL到MySQL数据迁移ID映射关系的业务操作，
 * 支持保存、查询、存在性检查、删除等功能，
 * 支持根据pgsqlId+type和mysqlId+type进行双向操作。
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
public interface DataMigrationMappingService {

    /**
     * 保存映射关系
     * 
     * 如果映射关系已存在则更新，不存在则新增。
     * 
     * @param pgsqlId PostgreSQL数据库中的主键ID
     * @param mysqlId MySQL数据库中的主键ID
     * @param busType 业务类型
     * @return 保存成功返回true，失败返回false
     */
    boolean saveMapping(String pgsqlId, Long mysqlId, MigrationBusTypeEnum busType);

    /**
     * 根据PostgreSQL ID和业务类型查询映射关系
     * 
     * @param pgsqlId PostgreSQL数据库中的主键ID
     * @param busType 业务类型
     * @return 映射关系实体，如果不存在返回Optional.empty()
     */
    Optional<DataMigrationMappingEntity> findByPgsqlIdAndType(String pgsqlId, MigrationBusTypeEnum busType);

    /**
     * 根据PostgreSQL ID和业务类型查询映射关系
     *
     * @param pgsqlIds PostgreSQL数据库中的主键ID
     * @param busType 业务类型
     * @return 映射关系实体，如果不存在返回Optional.empty()
     */
    List<DataMigrationMappingEntity> findByPgsqlIdsAndType(List<String> pgsqlIds, MigrationBusTypeEnum busType);

    /**
     * 根据MySQL ID和业务类型查询映射关系
     * 
     * @param mysqlId MySQL数据库中的主键ID
     * @param busType 业务类型
     * @return 映射关系实体，如果不存在返回Optional.empty()
     */
    Optional<DataMigrationMappingEntity> findByMysqlIdAndType(Long mysqlId, MigrationBusTypeEnum busType);

    /**
     * 检查PostgreSQL ID和业务类型的映射关系是否存在
     * 
     * @param pgsqlId PostgreSQL数据库中的主键ID
     * @param busType 业务类型
     * @return 存在返回true，不存在返回false
     */
    boolean existsByPgsqlIdAndType(String pgsqlId, MigrationBusTypeEnum busType);

    /**
     * 检查MySQL ID和业务类型的映射关系是否存在
     * 
     * @param mysqlId MySQL数据库中的主键ID
     * @param busType 业务类型
     * @return 存在返回true，不存在返回false
     */
    boolean existsByMysqlIdAndType(Long mysqlId, MigrationBusTypeEnum busType);

    /**
     * 根据PostgreSQL ID和业务类型删除映射关系
     * 
     * @param pgsqlId PostgreSQL数据库中的主键ID
     * @param busType 业务类型
     * @return 删除成功返回true，失败或不存在返回false
     */
    boolean deleteByPgsqlIdAndType(String pgsqlId, MigrationBusTypeEnum busType);

    /**
     * 根据MySQL ID和业务类型删除映射关系
     * 
     * @param mysqlId MySQL数据库中的主键ID
     * @param busType 业务类型
     * @return 删除成功返回true，失败或不存在返回false
     */
    boolean deleteByMysqlIdAndType(Long mysqlId, MigrationBusTypeEnum busType);

    /**
     * 批量保存映射关系
     * 
     * @param mappings 映射关系列表
     * @return 保存成功的数量
     */
    int saveBatchMappings(List<DataMigrationMappingEntity> mappings);

    /**
     * 根据业务类型查询所有映射关系
     * 
     * @param busType 业务类型
     * @return 映射关系列表
     */
    List<DataMigrationMappingEntity> findAllByType(MigrationBusTypeEnum busType);

    /**
     * 根据业务类型统计映射关系数量
     * 
     * @param busType 业务类型
     * @return 映射关系数量
     */
    long countByType(MigrationBusTypeEnum busType);

    /**
     * 根据 ID 更新映射记录
     *
     * @param entity 要更新的映射记录实体
     * @return 更新成功返回true，失败返回false
     */
    boolean updateById(DataMigrationMappingEntity entity);

    /**
     * 保存映射记录实体
     *
     * @param entity 要保存的映射记录实体
     * @return 保存成功返回true，失败返回false（保存成功后实体的ID会被自动填充）
     */
    boolean save(DataMigrationMappingEntity entity);

    /**
     * 根据业务类型查询所有映射关系
     *
     * @param busType 业务类型
     * @return 映射关系列表
     */
    List<DataMigrationMappingEntity> findAllByTypeAndDate(MigrationBusTypeEnum busType, LocalDateTime startDate);
}
