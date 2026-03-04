package com.item.framework.constant;

/**
 * Constants for Job Approval System
 * 
 * @author system
 * @since 1.0.0
 */
public final class JobApprovalConstants {
    
    // Email Retry Configuration
    public static final int MAX_EMAIL_RETRY_COUNT = 2;
    public static final int EMAIL_RETRY_INTERVAL_MINUTES = 30;
    
    // Comment Validation
    public static final int MIN_COMMENT_LENGTH = 20;
    public static final int MAX_COMMENT_LENGTH = 500;
    
    // Email Retry Status
    public static final String EMAIL_RETRY_STATUS_PENDING = "PENDING";
    public static final String EMAIL_RETRY_STATUS_COMPLETED = "COMPLETED";
    public static final String EMAIL_RETRY_STATUS_FAILED = "FAILED";
    
    // Email Template Names
    public static final String EMAIL_TEMPLATE_APPROVED = "job-approved";
    public static final String EMAIL_TEMPLATE_DENIED = "job-denied";
    public static final String EMAIL_TEMPLATE_CHANGES_REQUESTED = "job-changes-requested";
    public static final String EMAIL_TEMPLATE_APPROVAL_REQUEST = "job-approval-request";
    public static final String EMAIL_TEMPLATE_PUBLISHED = "job-published";
    
    // Email Subject Prefix
    public static final String EMAIL_SUBJECT_PREFIX = "Job Approval Notification - ";
    
    // Private constructor to prevent instantiation
    private JobApprovalConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
