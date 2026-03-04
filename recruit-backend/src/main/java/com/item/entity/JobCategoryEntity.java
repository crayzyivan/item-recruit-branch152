package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
@Data
@TableName(value = "r_job_category")
public class JobCategoryEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private int id;
    @TableField(value = "category_name")
    private String name;
    private String chineseName;
    private String spanishName;
    private String japaneseName;
}
