package com.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SalaryType DTO
 * 
 * @author system
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryTypeDTO implements Serializable {
    private Integer id;
    /**
     * Salary type code
     */
    private Integer code;
    
    /**
     * Salary type description
     */
    private String description;

    public static SalaryTypeDTO convertFromType(DictionaryDTO salaryType) {
        return new SalaryTypeDTO(salaryType.getId().intValue(), salaryType.getId().intValue(), salaryType.getValue());
    }
} 