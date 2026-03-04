package com.item.dto.job;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 候选人职位申请DTO
 *
 * @author lh
 * @since 2025-07-07
 */
@Data
public class CandidateJobApplyRequestVO {

    /**
     * 候选人ID
     * 必填，不能为空
     */
    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    /**
     * 职位ID
     * 必填，不能为空
     */
    @NotNull(message = "Job ID is required")
    private Long jobId;

    /**
     * 求职信
     * 可选，最大长度1000字符
     */
    @Size(max = 1000, message = "Cover letter must not exceed 1000 characters")
    private String coverLetter;

//    @NotNull(message = "jobCountryId ID is required")
    @Min(value = 1, message = "jobCountryId must be greater than or equal to {value}")
    private Long jobCountryId;
//    @NotNull(message = "jobStateId ID is required")
//    @Min(value = 1, message = "jobStateId must be greater than or equal to {value}")
    private Long jobStateId;
//    @NotNull(message = "jobCityId ID is required")
//    @Min(value = 1, message = "jobCityId must be greater than or equal to {value}")
    private Long jobCityId;

    /**
     * 对应 Google Map Place Id
     */
    private String placeId;

    private String jobCountryName;
    private String jobStateName;
    private String jobCityName;
    /**
     * 对应 Google Map Place Description
     */
    private String placeName;

    /**
     * 候选人期望 AI 电话面试开始时间，如：2025-12-11T17:00:00+08:00
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime preferredInterviewStartTime;

    /**
     * 候选人期望 AI 电话面试结束时间，如：2025-12-11T18:00:00+08:00
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime preferredInterviewEndTime;

    /**
     * 面试电话
     */
    @JsonProperty("phone")
    private String interviewPhone;

    /**
     * 电话面试语音
     */
    @JsonProperty("interviewLanguage")
    private String interviewLanguage;

} 