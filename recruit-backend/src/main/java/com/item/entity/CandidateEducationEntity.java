package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("r_candidate_education")
public class CandidateEducationEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long candidateId;
    private Long institutionTypeId;
    private String institutionName;
    private Integer graduated;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long degreeId;
    private String major;
    private String minor;
} 