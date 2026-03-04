package com.item.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : lh
 * r_job_mode 表的数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationTypeVO {
    /**
     * locationType id
     */
    private Integer modeId;
    /**
     * locationTypeName
     */
    private String modeName;
}
