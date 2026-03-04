package com.item.vo;

import com.item.dto.SalaryTypeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * SalaryType VO
 *
 * @author system
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryTypeVO implements Serializable {
    private Integer id;

    /**
     * Salary type description
     */
    private String description;

    public static List<SalaryTypeVO> convert2VO(List<SalaryTypeDTO> salaryTypes) {
        return salaryTypes.stream().map(type -> new SalaryTypeVO(type.getId(), type.getDescription())).toList();
    }

} 