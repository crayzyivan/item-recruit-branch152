package com.item.framework.constant;

/**
 * @author : lh
 */
public enum JobResponseCode implements IGlobalStatusCode {
    JOB_REQUIREMENT_EMPTY(100, "Job Requirement Can't be empty."),
    JOB_PUBLISH_FAIL(101, "Job publish fail."),
    POST_JOB_LATER(102, "Post a job later."),
    JOB_NOT_FOUND(103, "Not found job."),
    JOB_STATUS_NOT_ACTIVE(104, "The status of the current job is not open."),
    JOB_UPDATE_FAIL(105, "Job edit fail."),
    JOB_LINK_FAIL(106, "Job link fail."),
    JOB_DELETE_FAIL(107, "Job delete fail."),
    JOB_NO_PERMISSION(108, "No permission to perform this operation on the job."),
    THE_ACCOUNT_HAS_EXPIRED(300, "The account has expired."),
    THE_ACCOUNT_NO_POINTS(301, "There are no points in the account."),
    JOB_SEARCH_FAIL(302, "Job search fail."),
    JOB_ALREADY_EXISTS(303, "Job with same company name and title already exists."),
    JOB_FIELD_VALIDATED_FAIL(600, "{0} fields too long,Must not exceed {1}."),
    JOB_CURRENCY_NOT_FOUND(601, "Job currency not exist."),
    JOB_LOCATION_TYPE_NOT_FOUND(602, "Job location type not exist."),
    JOB_SALARY_TYPE_NOT_FOUND(603, "Job salary type not exist."),
    JOB_LOCATION_IS_NULL(700, "Job location must not be null."),
    JOB_LOCATION_DATA_INVALID(701, "Job location data not be invalid."),
    JOB_UPDATE_INSUFFICIENT_POINTS(702, "Insufficient points. Please top up and try again."),
    INTELLIGENCE_RULES_REQUIRED(720, "Score rules are required when intelligence switch is enabled."),
    INTELLIGENCE_RULES_MAX_LIMIT_SUM_WEIGHT(730, "The sum of weights configured for the current stage is abnormal."),
    INTERVIEW_TYPE_NU_SUPPORT_INTELLIGENCE_SWITCH(735, "The interview type of the current job is not supported intelligence determine."),
    JOB_INTERVIEW_LENGTH_INVALID(703, "Interview length must be at least 10 minutes for video interviews."),
    JOB_INTERVIEW_TYPE_CANNOT_MODIFY_WITH_APPLICATIONS(704, "Cannot modify interview type when there are candidate applications for this job."),
    JOB_AI_AUDIO_WRITTEN_TEST_NOT_ALLOWED(705, "Written test cannot be enabled for AI phone interviews."),
    JOB_EDIT_NOT_ALLOWED(706, "Job edit is not allowed in the current status. Only jobs in Pending Modification status can be edited."),
    INVALID_STATUS_TRANSITION(709, "Invalid status transition. This operation cannot be performed in the current status."),

    JOB_QUESTION_5S_TEST_NOT_ALLOWED(710, "Whether to enable 5S test questions. Editing is not allowed in the written test."),
    // 检查时间点相关验证
    CHECKPOINT_TIME_INVALID(735, "Checkpoint time must be less than or equal to interview length."),
    CHECKPOINT_SCORE_INVALID(710, "Checkpoint score threshold must be between 0 and 100."),
    CHECKPOINT_ONLY_FOR_VIDEO(711, "Checkpoint feature is only available for video interviews."),
    // 检查性格测试相关验证
    PERSONALITY_TEST_INVALID(712, "Personality test is only available for video interviews."),
    // 全部启用自定义问题
    CUSTOM_QUESTION_INVALID(713, "Custom questions cannot be empty when all are enabled."),
    CUSTOM_QUESTION_SIZE_INVALID(714, "Custom questions cannot exceed 20."),
    // 经纬度验证
    COORDINATES_REQUIRED(715, "Latitude and longitude parameters must be provided together."),
    LATITUDE_OUT_OF_RANGE(716, "Latitude must be between -90 and 90."),
    LONGITUDE_OUT_OF_RANGE(717, "Longitude must be between -180 and 180."),
    ;

    private final int statusCode;

    private final String message;

    JobResponseCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public int getCode() {
        return JOB_MODULE + this.statusCode;
    }

    @Override
    public String getMsg() {
        return this.message;
    }
}
