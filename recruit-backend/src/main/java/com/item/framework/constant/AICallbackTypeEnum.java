package com.item.framework.constant;

public enum AICallbackTypeEnum {
    COMPLETION("interview_ended"),
    REPORT( "interview_analysis_completed"),
    PDF_REPORT( "report"),
    CHEATING_ANALYSIS_COMPLETED( "cheating_analysis_completed"),
    SUMMARIZED_VIDEO_URL_UPDATED( "summarized_video_url_updated"),
    WRITTEN_TEST_ANALYSIS_COMPLETED( "written_test_analysis_completed"),
    CAMER_RECORDING_URL( "camera_recording_url_updated"),
    PHONE_INTERVIEW_COMPLETED("phone_interview_completed"),
    PHONE_INTERVIEW_NOT_COMPLETED("phone_interview_incomplete"),
    PHONE_INTERVIEW_NOTE_CONNECTED("phone_interview_not_connected"),
    PHONE_INTERVIEW_CANCELLED("phone_interview_cancelled"),
    ;

    private String name;

    AICallbackTypeEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
