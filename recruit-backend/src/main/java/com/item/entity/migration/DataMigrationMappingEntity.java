package com.item.entity.migration;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@TableName("r_data_migration_pgsql_mysql_id")
public class DataMigrationMappingEntity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * PostgreSQL库中主键ID
     */
    @TableField("pgsql_id")
    private String pgsqlId;

    /**
     * MySQL库中表主键ID
     */
    @TableField("mysql_id")
    private Long mysqlId;

    /**
     * 业务类型 (1: job表; 2: candidate表等)
     */
    @TableField("bus_type")
    private Integer busType;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除标识 (0: 未删除; 1: 已删除)
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    private String ext;
}
