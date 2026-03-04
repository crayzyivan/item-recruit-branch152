package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

@RefreshScope
@Component
@Data
@ConfigurationProperties(prefix = "ai.interview")
public class AiInterviewConfig {
    private String organizationName;
    private String userId;
    private String organizationId;
    private String interviewerId;
    private Boolean isAnonymous;
    private String logoUrl;
    private int responseCount;
    private int timeDuration;
    private Integer questionNumber;
    private String calculateMatchRateUrl;
    private String createInterviewWithQuestions;
    private String interviewUrl;
    private String generateJobDescription;
    private String getCall;
    private String generateInterviewLink;
    private Integer proceedWithApplicationScore;
    private String pdfGenerateJobDescription;
    private String WrittenDetail;
    private String exportInterviewReport;
    private String updateInterviewDetails;
    private String resumeParsingUrl;
    private long interviewTimeLimit;
    private int pdfTimeOut;
    private String processInterviewReport;
    private String processPhoneInterviewReport;
    private String pdfReportPath = "recruit/interview_reports/interview_report_{call_id}.pdf";
    private String updateInterviewTypeUrl;
    private String getInterviewType;
    private String schedulePhoneInterview;
    private String sendSmsUrl;
    private String sendSmsPhone;
    private Map<String, String> sendRecallPhone;
    private String recallInterviewPhone;
    private int recallAfterMinutes;
    private int rejectAfterMinutes;
    private int interviewMailBeforeDay;
}
