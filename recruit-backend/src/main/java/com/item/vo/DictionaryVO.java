package com.item.vo;

import lombok.Data;

@Data
public class DictionaryVO {
    private Long id;
    private String type;
    private String code;
    private String value;
    private Integer sort;
    private String remark;
}