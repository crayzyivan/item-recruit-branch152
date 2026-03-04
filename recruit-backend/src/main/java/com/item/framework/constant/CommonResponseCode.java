package com.item.framework.constant;

/**
 * @author : lh
 */
public enum CommonResponseCode implements IGlobalStatusCode {
    COMMON_COMPANY_LOGO_UPLOAD_FILE_EMPTY(100, "File is empty."),
    COMMON_COMPANY_LOGO_UPLOAD_FILE_SIZE(200, "File size exceeds limit of 256KB."),
    COMMON_COMPANY_LOGO_UPLOAD_FILE_FORMAT(300, "Unsupported file format. Only PNG, JPG/JPEG, and GIF are allowed."),
    COMMON_COMPANY_LOGO_UPLOAD_FILE_FAIL(400, "Upload logo file failed."),
    COMMON_REQUEST_PARAM_FORMAT_FAIL(500, "The request data format is incorrect."),
    COMMON_METHOD_PARAM_IS_NULL(600, "Method parameter is null."),
    COMMON_INTERFACE_UPGRADE_MAINTENANCE(601, "Interface upgrade and maintenance."),

    COMMON_CUSTOMER_CODE_OR_ID_IS_NULL(700, "customer code is null."),
    COMMON_MODULE_IS_NULL(701, "module is null."),
    COMMON_PAYMENT_ID_IS_NULL(702, "payment id is null."),
    COMMON_CREATE_LEADS_DUPLICATE(800, "Try again later."),
    COMMON_CUSTOMER_IS_NULL(900, "customer info is null."),


    COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER(1000, "Frequent operation, please try again later."),
    COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER_SECONDS(1006, "Frequent operation, please wait %s seconds before trying again."),
    COMMON_ERROR_MANY_LOCK(1010, "Too many errors, the account has been locked %s minutes."),
    COMMON_ERROR_MANY_ACCOUNT_LOCK(1011, "Account has been locked, please try again later."),
    COMMON_VERIFY_CODE_NULL(1030, "The verification code has expired, please re-acquire it."),
    COMMON_OPERATION_FAILED(1040, "Operation failed, please try again later."),

    COMMON_DATA_NOT_INVALID(2000, "Data not invalid."),
    COMMON_USER_IDENTIFY_NOT_INVALID(2010, "User identify not invalid."),

    // RSA加密解密相关错误码
    COMMON_RSA_ENCRYPT_FAIL(3000, "RSA encryption failed."),
    COMMON_RSA_DECRYPT_FAIL(3001, "RSA decryption failed."),
    COMMON_RSA_KEY_GENERATE_FAIL(3002, "RSA key generation failed."),
    COMMON_RSA_KEY_NOT_FOUND(3003, "RSA key not found."),
    COMMON_RSA_INVALID_KEY(3004, "Invalid RSA key."),
    COMMON_RSA_DATA_TOO_LONG(3005, "Data too long for RSA encryption."),


    POINTS_TRANSFER_FAILED(2501,"Point transfer failed. Please try again later."),
    POINTS_RECHARGE_FAILED(2502,"Failed to recharge points. Please try again later."),
    POINTS_DOWNLOAD_FAILED(2503,"Failed to download resume. Please try again later."),
    POINTS_PUBLISH_JOB_FAILED(2504,"Failed to post the job. Please try again later."),
    POINTS_AI_INTERVIEW_FAILED(2505,"Failed to post the job. Please try again later."),
    POINTS_TOP_UP_FAIL(2506,"Top up points fail"),
    INTERVIEW_ID_FAILED(2507,"interview id is empty. Please try again later."),
    INTERVIEW_EMAIL_FAILED(2508,"Failed to send the interview email. Please try again later."),
    CENTER_POINTS_FAILED(2509,"Failed to retrieve points information. Please try again later."),

    BACKGROUND_CHECK_FAILED(706,"Background check failed. Please try again later or contact support."),
    BIRTHDAY_MISSING_BACKGROUND_CHECK_FAILED(707, "Birthday information is missing. Unable to perform background check."),

    PDF_GENERATION_FAILED(707,"Generation failed. Please try again later."),

    CURRENT_USER_EXIST_REGISTER(4000, "The current user has been registered"),
    CURRENT_USER_INFO_EXCEPTION(4010, "The current user info exception"),

    INTERFACE_REQUEST_PARAM_VALIDATION(5000, "Interface request param validation failed."),

    // XML Feed related errors
    COMMON_XML_FEED_CONFIG_NOT_SET(6000, "XML Feed configuration not set. Please configure XML Feed first."),
    COMMON_XML_FEED_UPDATE_TOO_FREQUENT(6001, "Operation too frequent, please wait 2 minutes before trying again."),
    COMMON_XML_FEED_EMAIL_DUPLICATE(6002, "This email is already used by another company for Indeed configuration."),
    COMMON_XML_FEED_PLATFORM_TYPE_INVALID(6003, "Invalid platform type. Only LinkedIn and Indeed platforms are supported."),
    COMMON_XML_FEED_EMAIL_REQUIRED(6004, "Email address is required for configuration."),
    COMMON_XML_FEED_UPDATE_INTERVAL_INVALID(6005, "Update interval must be between 1 and 24 hours."),
    COMMON_XML_FEED_GENERATION_FAILED(6006, "Failed to generate XML feed. Please try again later."),
    COMMON_XML_FEED_S3_UPLOAD_FAILED(6007, "Failed to upload XML feed to storage. Please try again later."),
    COMMON_XML_FEED_MANUAL_UPDATE(6008, "Manual operation failed. Please contact the administrator."),
    COMMON_GENERAL_ERROR(6009, "Failed to generate XML."),

    COMMON_AYRSHARE_APIKEY_VALIDATE_FAIL(7000, "AyrShare api key validate failed. please confirm whether it is normal."),
    COMMON_AYRSHARE_APIKEY_NOT_FOUND(7005, "AyrShare api key not found."),
    COMMON_AYRSHARE_API_CALL_FAIL(7010, "AyrShare api calling fail."),
    COMMON_AYRSHARE_NOT_LINKED_ANY_PLATFORM(7015, "AyrShare not linked any platform."),

    ES_JOB_LIST_SEARCH_FAIL(7016, "Elasticsearch query failed while fetching job list."),
    SEARCH_RESUME_CANDIDATE(7017,"Failed to search candidates from ES"),

    // Interview Result Sync related errors
    INTERVIEW_RESULT_NOT_FOUND(8001, "Interview result not found in PostgreSQL."),
    INTERVIEW_RESULT_SYNC_FAILED(8002, "Failed to sync interview result to MySQL."),
    INTERVIEW_RESULT_ALREADY_EXISTS(8003, "Interview result already exists in MySQL."),
    INTERVIEW_RESULT_CONVERSION_FAILED(8004, "Failed to convert interview result data."),

    CURRENT_USER_SWITCH_COMPANY(9000, "The current user is not allowed to switch to this tenant."),
    ;

    private final int statusCode;

    private final String message;

    CommonResponseCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public int getCode() {
        return COMMON_MODULE + this.statusCode;
    }

    @Override
    public String getMsg() {
        return this.message;
    }
}
