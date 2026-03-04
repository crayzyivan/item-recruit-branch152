package com.item.service.migration.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.mapper.migration.DataMigrationMappingMapper;
import com.item.service.migration.DataMigrationMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 数据迁移映射服务实现类
 * 
 * 实现PostgreSQL到MySQL数据迁移ID映射关系的业务操作，
 * 使用MyBatis Plus的LambdaQueryWrapper实现类型安全的复杂查询逻辑，
 * 处理数据的保存、更新、查询、删除操作。
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@Service
public class DataMigrationMappingServiceImpl extends ServiceImpl<DataMigrationMappingMapper, DataMigrationMappingEntity> implements DataMigrationMappingService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMapping(String pgsqlId, Long mysqlId, MigrationBusTypeEnum busType) {
        if (StringUtils.isBlank(pgsqlId) || mysqlId == null || busType == null) {
            log.warn("saveMapping failed: invalid parameters - pgsqlId: {}, mysqlId: {}, busType: {}", 
                    pgsqlId, mysqlId, busType);
            return false;
        }

        try {
            // 先查询是否存在
            LambdaQueryWrapper<DataMigrationMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DataMigrationMappingEntity::getPgsqlId, pgsqlId)
                       .eq(DataMigrationMappingEntity::getBusType, busType.getCode());
            
            List<DataMigrationMappingEntity> existingList = this.list(queryWrapper);
            DataMigrationMappingEntity existing = CollectionUtils.isNotEmpty(existingList) ? existingList.get(0) : null;
            
            if (existing != null) {
                // 存在则更新
                existing.setMysqlId(mysqlId);
                existing.setCreateTime(LocalDateTime.now());
                boolean updateResult = this.updateById(existing);
                log.info("Updated mapping: pgsqlId={}, mysqlId={}, busType={}, result={}", 
                        pgsqlId, mysqlId, busType, updateResult);
                return updateResult;
            } else {
                // 不存在则插入
                DataMigrationMappingEntity entity = new DataMigrationMappingEntity();
                entity.setPgsqlId(pgsqlId);
                entity.setMysqlId(mysqlId);
                entity.setBusType(busType.getCode());
                entity.setCreateTime(LocalDateTime.now());
                
                boolean insertResult = this.save(entity);
                log.info("Inserted mapping: pgsqlId={}, mysqlId={}, busType={}, result={}", 
                        pgsqlId, mysqlId, busType, insertResult);
                return insertResult;
            }
        } catch (Exception e) {
            log.error("saveMapping failed: pgsqlId={}, mysqlId={}, busType={}", 
                    pgsqlId, mysqlId, busType, e);
            return false;
        }
    }

    @Override
    public Optional<DataMigrationMappingEntity> findByPgsqlIdAndType(String pgsqlId, MigrationBusTypeEnum busType) {
        if (StringUtils.isBlank(pgsqlId) || busType == null) {
            log.warn("findByPgsqlIdAndType failed: invalid parameters - pgsqlId: {}, busType: {}", 
                    pgsqlId, busType);
            return Optional.empty();
        }

        try {
            LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DataMigrationMappingEntity::getPgsqlId, pgsqlId)
                   .eq(DataMigrationMappingEntity::getBusType, busType.getCode());
            
            List<DataMigrationMappingEntity> entityList = this.list(wrapper);
            DataMigrationMappingEntity entity = CollectionUtils.isNotEmpty(entityList) ? entityList.get(0) : null;
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            log.error("findByPgsqlIdAndType failed: pgsqlId={}, busType={}", pgsqlId, busType, e);
            return Optional.empty();
        }
    }

    @Override
    public List<DataMigrationMappingEntity> findByPgsqlIdsAndType(List<String> pgsqlIds, MigrationBusTypeEnum busType) {
        if (CollectionUtils.isEmpty(pgsqlIds) || busType == null) {
            log.warn("findByPgsqlIdAndType failed: invalid parameters - pgsqlId: {}, busType: {}",
                    pgsqlIds, busType);
            return List.of();
        }

        try {
            LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(DataMigrationMappingEntity::getPgsqlId, pgsqlIds)
                   .eq(DataMigrationMappingEntity::getBusType, busType.getCode());

            List<DataMigrationMappingEntity> entityList = this.list(wrapper);
            entityList = CollectionUtils.isNotEmpty(entityList) ? entityList : List.of();
            return entityList;
        } catch (Exception e) {
            log.error("findByPgsqlIdAndType failed: pgsqlId={}, busType={}", pgsqlIds, busType, e);
            return List.of();
        }
    }

    @Override
    public Optional<DataMigrationMappingEntity> findByMysqlIdAndType(Long mysqlId, MigrationBusTypeEnum busType) {
        if (mysqlId == null || busType == null) {
            log.warn("findByMysqlIdAndType failed: invalid parameters - mysqlId: {}, busType: {}", 
                    mysqlId, busType);
            return Optional.empty();
        }

        try {
            LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DataMigrationMappingEntity::getMysqlId, mysqlId)
                   .eq(DataMigrationMappingEntity::getBusType, busType.getCode());
            
            List<DataMigrationMappingEntity> entityList = this.list(wrapper);
            DataMigrationMappingEntity entity = CollectionUtils.isNotEmpty(entityList) ? entityList.get(0) : null;
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            log.error("findByMysqlIdAndType failed: mysqlId={}, busType={}", mysqlId, busType, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByPgsqlIdAndType(String pgsqlId, MigrationBusTypeEnum busType) {
        if (StringUtils.isBlank(pgsqlId) || busType == null) {
            return false;
        }

        try {
            LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DataMigrationMappingEntity::getPgsqlId, pgsqlId)
                   .eq(DataMigrationMappingEntity::getBusType, busType.getCode());
            
            long count = this.count(wrapper);
            return count > 0;
        } catch (Exception e) {
            log.error("existsByPgsqlIdAndType failed: pgsqlId={}, busType={}", pgsqlId, busType, e);
            return false;
        }
    }

    @Override
    public boolean existsByMysqlIdAndType(Long mysqlId, MigrationBusTypeEnum busType) {
        if (mysqlId == null || busType == null) {
            return false;
        }

        try {
            LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DataMigrationMappingEntity::getMysqlId, mysqlId)
                   .eq(DataMigrationMappingEntity::getBusType, busType.getCode());
            
            long count = this.count(wrapper);
            return count > 0;
        } catch (Exception e) {
            log.error("existsByMysqlIdAndType failed: mysqlId={}, busType={}", mysqlId, busType, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByPgsqlIdAndType(String pgsqlId, MigrationBusTypeEnum busType) {
        if (StringUtils.isBlank(pgsqlId) || busType == null) {
            log.warn("deleteByPgsqlIdAndType failed: invalid parameters - pgsqlId: {}, busType: {}", 
                    pgsqlId, busType);
            return false;
        }

        try {
            LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DataMigrationMappingEntity::getPgsqlId, pgsqlId)
                   .eq(DataMigrationMappingEntity::getBusType, busType.getCode());
            
            boolean deleteResult = this.remove(wrapper);
            log.info("Deleted mapping by pgsqlId: pgsqlId={}, busType={}, result={}", 
                    pgsqlId, busType, deleteResult);
            return deleteResult;
        } catch (Exception e) {
            log.error("deleteByPgsqlIdAndType failed: pgsqlId={}, busType={}", pgsqlId, busType, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByMysqlIdAndType(Long mysqlId, MigrationBusTypeEnum busType) {
        if (mysqlId == null || busType == null) {
            log.warn("deleteByMysqlIdAndType failed: invalid parameters - mysqlId: {}, busType: {}", 
                    mysqlId, busType);
            return false;
        }

        try {
            LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DataMigrationMappingEntity::getMysqlId, mysqlId)
                   .eq(DataMigrationMappingEntity::getBusType, busType.getCode());
            
            boolean deleteResult = this.remove(wrapper);
            log.info("Deleted mapping by mysqlId: mysqlId={}, busType={}, result={}", 
                    mysqlId, busType, deleteResult);
            return deleteResult;
        } catch (Exception e) {
            log.error("deleteByMysqlIdAndType failed: mysqlId={}, busType={}", mysqlId, busType, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveBatchMappings(List<DataMigrationMappingEntity> mappings) {
        if (CollectionUtils.isEmpty(mappings)) {
            log.warn("saveBatchMappings failed: mappings list is null or empty");
            return 0;
        }

        try {
            int successCount = 0;
            for (DataMigrationMappingEntity mapping : mappings) {
                if (mapping.getPgsqlId() != null && mapping.getMysqlId() != null && mapping.getBusType() != null) {
                    try {
                        // 直接使用ServiceImpl操作，避免调用事务方法
                        LambdaQueryWrapper<DataMigrationMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(DataMigrationMappingEntity::getPgsqlId, mapping.getPgsqlId())
                                   .eq(DataMigrationMappingEntity::getBusType, mapping.getBusType());
                        
                        List<DataMigrationMappingEntity> existingList = this.list(queryWrapper);
                        DataMigrationMappingEntity existing = CollectionUtils.isNotEmpty(existingList) ? existingList.get(0) : null;
                        
                        if (existing != null) {
                            existing.setMysqlId(mapping.getMysqlId());
                            if (this.updateById(existing)) {
                                successCount++;
                            }
                        } else {
                            if (this.save(mapping)) {
                                successCount++;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to save single mapping: pgsqlId={}, mysqlId={}, busType={}", 
                                mapping.getPgsqlId(), mapping.getMysqlId(), mapping.getBusType(), e);
                    }
                }
            }
            log.info("Batch saved mappings: total={}, success={}", mappings.size(), successCount);
            return successCount;
        } catch (Exception e) {
            log.error("saveBatchMappings failed: mappings size={}", mappings.size(), e);
            return 0;
        }
    }

    @Override
    public List<DataMigrationMappingEntity> findAllByType(MigrationBusTypeEnum busType) {
        if (busType == null) {
            log.warn("findAllByType failed: busType is null");
            return new ArrayList<>();
        }

        try {
            LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DataMigrationMappingEntity::getBusType, busType.getCode())
                   .orderByDesc(DataMigrationMappingEntity::getCreateTime);
            
            return this.list(wrapper);
        } catch (Exception e) {
            log.error("findAllByType failed: busType={}", busType, e);
            return new ArrayList<>();
        }
    }

    @Override
    public long countByType(MigrationBusTypeEnum busType) {
        if (busType == null) {
            return 0L;
        }

        try {
            LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DataMigrationMappingEntity::getBusType, busType.getCode());
            
            return this.count(wrapper);
        } catch (Exception e) {
            log.error("countByType failed: busType={}", busType, e);
            return 0L;
        }
    }

    @Override
    public boolean updateById(DataMigrationMappingEntity entity) {
        if (entity == null || entity.getId() == null) {
            log.warn("updateById failed: entity or entity.id is null");
            return false;
        }

        try {
            boolean updated = super.updateById(entity);

            if (updated) {
                log.debug("updateById success: id={}, pgsqlId={}, mysqlId={}, busType={}",
                    entity.getId(), entity.getPgsqlId(), entity.getMysqlId(), entity.getBusType());
            } else {
                log.warn("updateById failed: update operation failed - id={}", entity.getId());
            }

            return updated;
        } catch (Exception e) {
            log.error("updateById exception: id={}", entity.getId(), e);
            return false;
        }
    }

    @Override
    public boolean save(DataMigrationMappingEntity entity) {
        if (entity == null) {
            log.warn("save failed: entity is null");
            return false;
        }

        try {
            // 调用父类的save方法，MyBatis Plus会自动填充ID
            boolean saved = super.save(entity);

            if (saved) {
                log.debug("save success: id={}, pgsqlId={}, mysqlId={}, busType={}",
                    entity.getId(), entity.getPgsqlId(), entity.getMysqlId(), entity.getBusType());
            } else {
                log.warn("save failed: insert operation failed");
            }

            return saved;
        } catch (Exception e) {
            log.error("save exception: pgsqlId={}, busType={}", entity.getPgsqlId(), entity.getBusType(), e);
            return false;
        }
    }

    /**
     * 根据业务类型查询所有映射关系
     *
     * @param busType 业务类型
     * @return 映射关系列表
     */
    @Override
    public List<DataMigrationMappingEntity> findAllByTypeAndDate(MigrationBusTypeEnum busType, LocalDateTime startDate) {
        LambdaQueryWrapper<DataMigrationMappingEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataMigrationMappingEntity::getBusType, busType.getCode());
        if (startDate!=null){
            wrapper.ge(DataMigrationMappingEntity::getCreateTime, startDate);
        }
        wrapper.orderByDesc(DataMigrationMappingEntity::getCreateTime);
        return this.list(wrapper);
    }
}
