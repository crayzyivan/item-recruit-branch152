package com.item.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO：功能描述
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-12-22  15:37
 */
@Data
public class CandidateJobRecordVO {

    private Long id;

    private Long candidateId;

    private Long jobId;

    private String candidateName;

    private String jobTitle;

    private String countryName;
    private String stateName;
    private String cityName;
    private String placeName;

    private Integer applyStatus;
    private String applyStatusName;
    private LocalDateTime createTime;
}