package com.item.dto;

import com.item.vo.CandidateScoreVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ai面试结果
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:38
 */
@Data
public class AiVettedResultDTO {
    private Long id;
    private Long candidateId;
    private Long reportId;
    private String candidateName;
    private String candidateEmail;
    private LocalDateTime interviewTime;
    private Integer interviewScore;
    private Integer proctoringScore;
    private Integer interviewType;

    List<AiVettedResultSkillDTO> skillList;
    //候选人各项评分
    private CandidateScoreVO candidateScore;

}