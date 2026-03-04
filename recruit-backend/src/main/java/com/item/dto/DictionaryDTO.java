package com.item.dto;

import lombok.Data;

@Data
public class DictionaryDTO {
    private Long id;
    private String type;
    private String code;
    private String value;
    private Integer sort;
    private String remark;
    private Integer status;
} 