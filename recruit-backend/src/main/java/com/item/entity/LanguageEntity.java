package com.item.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * ISO 639-1 语言代码标准表
 */
@Data
@TableName("r_languages")
public class LanguageEntity {

    @TableId
    private Integer id;

    private String code;
    @TableField("name_en")
    private String nameEn;
    @TableField("name_zh")
    private String nameZh;
    @TableField("native_name")
    private String nativeName;
}
