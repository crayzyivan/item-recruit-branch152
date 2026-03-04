package com.item.dto.crm;

import lombok.Builder;
import lombok.Data;

/**
 * @author : lh
 */
@Data
@Builder
public class LeadsPropertiesDTO {
    private String fieldName;
    private String displayName;
    private String value;
    private Long fieldId;
    private Long fieldType;
    private Boolean isHidden;
    private String description;
    private Integer sort;
}

