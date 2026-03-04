package com.item.vo.ai;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
@Data
public class AIJobResponseDataVO implements Serializable {
    private String overview;
    private List<String> responsibilities;
    private List<String> minimumRequirements;
    private List<String> preferredRequirements;
    private List<String> skills;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer salaryType;
    private Integer currency;
}
