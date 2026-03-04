package com.item.service.migration.location;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.migration.location.PgLocationEntity;

import java.util.List;

/**
 * PostgreSQL Location 服务接口
 *
 * 提供对 PostgreSQL 数据库中 companies.locations 表的业务操作。
 * 继承 MyBatis Plus 的 IService 接口，提供完整的 CRUD 操作。
 * 支持分页查询、条件查询、批量查询等复杂业务场景。
 *
 * @author system
 * @version 1.0
 * @since 2025-10-15
 */
public interface PgLocationService extends IService<PgLocationEntity> {

    /**
     * 根据主键IDs批量查询 Location 数据
     *
     * @param locationIds Location ID 列表
     * @return 匹配的 Location 实体列表
     */
    List<PgLocationEntity> getLocationsByIds(List<Integer> locationIds);
}
