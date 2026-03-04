package com.item.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ai面试报告
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-24  11:48
 */
@Data
public class InterviewReportVO {
    private Long id;

    private LocalDateTime interviewTime;
    private Integer interviewScore;
    private Integer proctoringScore;
    //性格分
    private Integer personalityScore;
    //是否开启性格测试
    private Boolean personalityTestEnabled;

    private String cameraRecordingUrl;
    private String summarizedVideoUrl;
    private String phoneRecordingUrl;

    List<AiVettedResultSkillVO> skillList;
    //软技能
    AiVettedResultSkillVO softSkill;

    private String overallSkillAssessment;
    private Integer overallSkillScore;
    private Integer overallSkillLevel;
    //面试记录
    private String transcript;

    private String callId;

    private String interviewReportUrl;
    private Integer interviewType;

}