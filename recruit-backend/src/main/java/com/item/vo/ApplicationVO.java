package com.item.vo;

import com.item.dto.AnswerQuestion5SInfoDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * application
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-02  10:58
 */
@Data
public class ApplicationVO {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;

    private String countryName;
    private String stateName;
    private String cityName;

    private String phoneNumber;

    private LocalDateTime sendTime;

    //是否显示邀请
    private Boolean displayReapply;

    private Integer applyStatus;
    private String applyStatusName;

    //候选人各项评分
    private CandidateScoreVO candidateScore;

    private AnswerQuestion5SInfoDTO answerQuestion5SInfo;
}