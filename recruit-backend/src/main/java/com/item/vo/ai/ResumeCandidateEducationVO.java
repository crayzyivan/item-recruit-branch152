package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeCandidateEducationVO {
    private String institutionType; //教育机构，需要根据字典表转换成id返回给前端
    private String institutionName;
    private Integer graduated;
    private LocalDate startDate;
    private LocalDate endDate;
    private String degree;  //学位,需要根据字典表转换成id返回给前端
    private String major;
    private String minor;
} 