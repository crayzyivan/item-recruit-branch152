package com.item.service.migration.location.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.migration.location.PgLocationEntity;
import com.item.pgmapper.migration.location.PgLocationMigrationMapper;
import com.item.service.migration.location.PgLocationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * PostgreSQL Location 服务实现类
 *
 * 实现对 PostgreSQL 数据库中 companies.locations 表的业务操作。
 * 继承 ServiceImpl 以便直接使用 MyBatis Plus 的 CRUD 方法。
 * 使用 LambdaQueryWrapper 实现类型安全的复杂查询逻辑。
 *
 * @author system
 * @version 1.0
 * @since 2025-10-15
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
public class PgLocationServiceImpl extends ServiceImpl<PgLocationMigrationMapper, PgLocationEntity> implements PgLocationService {

    @Override
    public List<PgLocationEntity> getLocationsByIds(List<Integer> locationIds) {
        if (CollectionUtils.isEmpty(locationIds)) {
            log.warn("getLocationsByIds failed: locationIds list is null or empty");
            return Collections.emptyList();
        }
        
        try {
            LambdaQueryWrapper<PgLocationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(PgLocationEntity::getId, locationIds);
            
            List<PgLocationEntity> locations = this.list(wrapper);
            log.debug("根据 IDs 批量查询 Location: locationIds={}, count={}", locationIds, locations.size());
            return locations;
        } catch (Exception e) {
            log.error("根据 IDs 批量查询 Location 失败: locationIds={}", locationIds, e);
            return Collections.emptyList();
        }
    }
}
