package com.item.pgentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * PostgreSQL货币实体
 * 对应common.currencies表
 * 
 * @author system
 */
@Data
@TableName("common.currencies")
public class PgCurrency {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 货币名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 货币符号
     */
    @TableField("symbol")
    private String symbol;
}
