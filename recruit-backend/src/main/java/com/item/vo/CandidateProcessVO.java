package com.item.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 候选人应聘流程
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-24  13:53
 */
@Data
public class CandidateProcessVO {
    private Long candidateId;
    private Long jobId;
    private String candidateName;
    private Integer expectedSalary;
    private String salaryType;
    private String currencyType;
    private String countryName;
    private String stateName;
    private String cityName;
    private LocalDate availableFrom;

    private String jobTitle;
    private String jobLocationName;
    private LocalDateTime jobCreateTime;


    List<ProcessTimeLineVO> timeLineList;
}