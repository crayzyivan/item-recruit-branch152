package com.item.entity.migration.category;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * PostgreSQL categories 表实体类
 * 
 * 映射 PostgreSQL common.categories 表，用于数据迁移相关操作。
 * 表结构：id (integer primary key), name (character varying not null)
 *
 * @author system
 * @since 2025-10-14
 */
@Data
@TableName("common.categories")
public class PgCategoryEntity implements Serializable {

    /**
     * 主键ID
     * 使用序列 categories_id_seq 自动生成
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 分类名称
     * 对应 PostgreSQL character varying not null 字段
     */
    @TableField("name")
    private String name;
}
