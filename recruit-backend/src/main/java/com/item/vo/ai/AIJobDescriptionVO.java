package com.item.vo.ai;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * pdf生成jd结果
 */
@Data
public class AIJobDescriptionVO implements Serializable {
    private String title;
    private List<LocationAIVO> locations;
    private String department;
    private String overview;
    private List<String> responsibilities;
    private List<String> minimumRequirements;
    private List<String> preferredRequirements;
    private List<String> skills;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer categoryId;
    private Integer typeId;

    private Integer modeId;

    private List<String> benefits;

    private Integer salaryType;

    private Integer currency;

    /**
     * 空缺数量  非必须
     */
    private Integer numberOpenings;

}
