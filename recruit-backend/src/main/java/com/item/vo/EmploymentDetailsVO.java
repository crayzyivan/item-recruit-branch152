package com.item.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 工作经历
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-25  14:50
 */
@Data
public class EmploymentDetailsVO {
    //公司名称
    private String companyName;
    //职位
    private String jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    //主要职责
    private String keyResponsibilities;
}