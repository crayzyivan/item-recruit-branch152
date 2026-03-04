package com.item.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/24
 * @since 1.0.0
 */
@Data
public class CandidateSimpleVO implements Serializable {
    private Long jobId;
    private Long candidateId;
    private String candidateName;
    private String location;
    private String jobTitle;
    private String jobLocation;
    private Integer expectedSalary;
    private Long salaryTypeId;
    private String salaryTypeName;
    private Integer applyStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
