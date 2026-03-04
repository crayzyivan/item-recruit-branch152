package com.item.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拒绝列表信息
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-23  10:58
 */
@Data
public class DeniedListVO {
    private Long id;
    /**
     * Candidate ID
     */
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

    private LocalDateTime rejectTime;
    private Integer assessmentScore;
    //候选人各项评分
    private CandidateScoreVO candidateScore;

}