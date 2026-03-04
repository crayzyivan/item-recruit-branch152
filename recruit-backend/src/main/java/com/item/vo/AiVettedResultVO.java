package com.item.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ai面试结果
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  19:08
 */
@Data
public class AiVettedResultVO {
    private Long id;
    private Long reportId;
    /**
     * Candidate ID
     */
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private LocalDateTime interviewTime;
    private Integer interviewScore;
    private Integer proctoringScore;
    private Integer interviewType;

    List<AiVettedResultSkillVO> skillList;
    //候选人各项评分
    private CandidateScoreVO candidateScore;
}