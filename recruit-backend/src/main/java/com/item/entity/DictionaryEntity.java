package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典表
 */
@Data
@TableName("r_dictionary")
public class DictionaryEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String code;
    private String value;
    private Integer sort;
    private String remark;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private String chineseName;
    private String spanishName;
    private String japaneseName;
} 