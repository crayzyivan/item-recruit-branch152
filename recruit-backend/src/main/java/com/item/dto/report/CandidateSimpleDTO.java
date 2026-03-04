package com.item.dto.report;

import com.item.dto.FullLocationDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/23
 * @since 1.0.0
 */
@Data
public class CandidateSimpleDTO implements Serializable {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String location;
    private String jobTitle;
    private String jobLocation;
    private Integer expectedSalary;
    private Long salaryTypeId;
    private String salaryTypeName;
    private Long currencyTypeId;
    private String currencyName;
    private Integer applyStatus;
    private String applyStatusName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private FullLocationDTO fullLocation;
    private LocalDateTime applyTime;
}
