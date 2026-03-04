package com.item.dto.ai;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 根据上传pdf文件生成的工作描述
 */
@Data
public class JobDescriptionDTO implements Serializable {
    private String title;
    private List<LocationAIDTO> workplace;
    private String department;
    private String overview;
    private List<String> responsibilities;
    private List<String> minimumRequirements;
    private List<String> preferredRequirements;
    private List<String> skills;
    private Integer minimumSalary;
    private Integer maximumSalary;
    private Integer categoryId;
    private String domain;
    private Integer typeId;
    private String jobType;
    private Integer modeId;
    private String workLocationType;
    private List<String> benefits;
    private Long salaryTypeId;
    private String salaryType;
    private Long currency;
    private String currencyType;
    /**
     * 空缺数量  非必须
     */
    private Integer numberOfPositions;

}
