package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 面试报告响应VO
 *
 * @author yarong.guo
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewReportResponseVO {
    
    @JsonProperty("interview_report_s3_key")
    private String interviewReportS3Key;
}
