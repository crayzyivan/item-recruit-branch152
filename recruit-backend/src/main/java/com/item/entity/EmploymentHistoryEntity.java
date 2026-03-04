package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("r_employment_history")
public class EmploymentHistoryEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long candidateId;
    private String companyName;
    private String currentEmployer;
    private String jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyResponsibilities;
} 