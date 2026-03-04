package com.item.dto.job;


import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 候选人职位申请DTO
 *
 * @author lh
 * @since 2025-07-07
 */
@Data
public class CandidateJobApplyDto {

    /**
     * 候选人ID
     * 必填，不能为空
     */
    private Long candidateId;

    /**
     * 职位ID
     * 必填，不能为空
     */
    private Long jobId;

    /**
     * 求职信
     * 可选，最大长度1000字符
     */
    private String coverLetter;


    private Long jobCountryId;
    private Long jobStateId;
    private Long jobCityId;
    private String jobCountryName;
    private String jobStateName;
    private String jobCityName;
    /**
     * Google Map Place Id
     */
    private String placeId;
    /**
     * 面试电话
     */
    private String interviewPhone;
    /**
     * 电话面试语言
     */
    private String interviewLanguage;
    /**
     * 候选人期望AI电话面试开始时间
     */
    private OffsetDateTime preferredInterviewStartTime;

    /**
     * 候选人期望AI电话面试结束时间
     */
    private OffsetDateTime preferredInterviewEndTime;
} 