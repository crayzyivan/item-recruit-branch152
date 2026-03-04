package com.item.dto.ai;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/19
 * @since 1.0.0
 */
@Data
public class JobResponseDataDTO implements Serializable {
    private String overview;
    private List<String> responsibilities;
    private List<String> minimumRequirements;
    private List<String> preferredRequirements;
    private List<String> skills;
    @JsonProperty("minimumSalary")
    private Integer minSalary;
    @JsonProperty("maximumSalary")
    private Integer maxSalary;
    private String currencyType;
    private Long currency;
    private Integer salaryType;
}
