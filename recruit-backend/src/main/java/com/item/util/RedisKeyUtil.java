package com.item.util;

/**
 * @author : lh
 */
public class RedisKeyUtil {

    private static final String LOCK_LEADS_CUSTOMER_COMPANY = "recruit:lock:leads:";
    private static final String LOCK_PUBLISH_JOB = "recruit:lock:publish:";
    private static final String LOCK_EDIT_JOB = "recruit:lock:edit:";
    private static final String LOCK_PRE_LOGIN_CHECK = "recruit:lock:pre-login:";
    private static final String LOCK_AYRSHARE_CONFIG = "recruit:lock:ayrshare:";
    private static final String LOCK_SEND_INTERVIEW_URL = "recruit:lock:send-interview-url:";

    public static String getCustomerCompanyKey(String companyCode) {
        return LOCK_LEADS_CUSTOMER_COMPANY + companyCode;
    }

    public static String getPublishJobKey(String companyCode) {
        return LOCK_PUBLISH_JOB + companyCode;
    }

    public static String getEditJobKey(Long jobId) {
        return LOCK_EDIT_JOB + jobId;
    }
    public static String getLockPreLoginCheckKey(String candidateId) {
        return LOCK_PRE_LOGIN_CHECK + candidateId;
    }

    public static String getLockAyrshareConfigKey(String companyCode) {
        return LOCK_AYRSHARE_CONFIG + companyCode;
    }

    public static String getLockSendInterviewUrl(Long candidateJobId) {
        return LOCK_SEND_INTERVIEW_URL + candidateJobId;
    }
}
