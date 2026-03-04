package com.item.dto.migration.job;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据迁移PostgreSQL-MySQL ID映射关系实体类
 * 
 * 用于管理PostgreSQL到MySQL数据迁移过程中的ID映射关系，
 * 支持不同业务类型的数据映射管理。
 *
 * @author system
 * @since 2025-09-30
 */
@Data
public class DataMigrationMappingDTO implements Serializable {

    /**
     * PostgreSQL库中主键ID
     */
    private String pgsqlId;

    /**
     * MySQL库中表主键ID
     */
    private Long mysqlId;

    /**
     * 业务类型 (1: job表; 2: candidate表等)
     */
    private Integer busType;

    /**
     * 逻辑删除标识 (0: 未删除; 1: 已删除)
     */
    private String ext;
}
