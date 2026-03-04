package com.item.dto.cache;

import lombok.Data;

/**
 * 字典表
 */
@Data
public class DictionaryCacheDTO {
    private Long id;
    private String type;
    private String code;
    private String value;
    private Integer sort;
    private String remark;
    private Integer status;
    private String chineseName;
    private String spanishName;
    private String japaneseName;
} 