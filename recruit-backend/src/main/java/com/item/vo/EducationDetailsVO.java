package com.item.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 教育经历
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-25  14:55
 */
@Data
public class EducationDetailsVO {

    //机构名称
    private String institutionName;
    private LocalDate startDate;
    private LocalDate endDate;

    private String major;
    private String degreeName;
}