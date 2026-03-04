package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallVO {
    @JsonProperty("callResponse")
    private CallResponseVO callResponse;
    @JsonProperty("analytics")
    private AnalyticsVO analytics;
    @JsonProperty("camera_recording_url")
    private String cameraRecordingUrl;
    @JsonProperty("summarized_video_url")
    private String summarizedVideoUrl;
    @JsonProperty("cheating_analysis")
    private CheatingAnalysisVO cheatingAnalysis;
    @JsonProperty("written_test_analysis")
    private WrittenTestAnalysisVO writtenTestAnalysis;
    /**
     * 面试类型 (0:video, 1:audio)
     */
    private String interviewType;
    @JsonProperty("interview_report_s3_key")
    private String interviewReportS3Key;
}
