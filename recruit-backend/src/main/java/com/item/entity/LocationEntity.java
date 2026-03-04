package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 未使用的类和表
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
@Deprecated
@Data
@TableName(value = "r_location")
public class LocationEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private int id;
    @TableField(value = "city_name")
    private String cityName;
}
