package com.item.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 候选人与职位关联信息
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-22  09:40
 */
@Data
public class CandidateJobVO {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;

    private Long countryId;
    private Long stateId;
    private Long cityId;

    private String countryName;
    private String stateName;
    private String cityName;

    private String phoneNumber;
    private Integer assessmentScore;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}