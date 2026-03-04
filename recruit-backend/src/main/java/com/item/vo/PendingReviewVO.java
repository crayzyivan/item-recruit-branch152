package com.item.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人工审核列表信息
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-23  10:58
 */
@Data
public class PendingReviewVO {
    private Long id;
    /**
     * Candidate ID
     */
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;

    private String countryName;
    private String stateName;
    private String cityName;

    private String phoneNumber;

    private LocalDateTime pendingReviewTime;

    //候选人各项评分
    private CandidateScoreVO candidateScore;
}