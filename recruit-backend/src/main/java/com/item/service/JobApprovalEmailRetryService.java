package com.item.service;

import com.item.entity.JobApprovalEmailRetryEntity;

import java.util.List;
import java.util.Map;

/**
 * Service interface for handling job approval email retry operations
 * 
 * @author system
 * @since 1.0.0
 */
public interface JobApprovalEmailRetryService {
    
    /**
     * Send email with retry mechanism
     * 
     * @param jobId Job ID
     * @param action Action type
     * @param comment Comment
     * @param templateName Email template name
     * @param variables Template variables
     * @param recipient Recipient email address
     */
    void sendEmailWithRetry(Long jobId, String action, String comment, 
                           String templateName, Map<String, Object> variables, 
                           String[] recipient);
    
    /**
     * Find emails that need retry
     * 
     * @return List of email retry entities that need retry
     */
    List<JobApprovalEmailRetryEntity> findEmailsToRetry();
    
    /**
     * Mark email retry as completed
     * 
     * @param retryId Retry entity ID
     */
    void markAsCompleted(Long retryId);
    
    /**
     * Update retry count
     * 
     * @param retryId Retry entity ID
     * @param retryCount New retry count
     */
    void updateRetryCount(Long retryId, Integer retryCount);
    
    /**
     * Mark email retry as failed
     * 
     * @param retryId Retry entity ID
     */
    void markAsFailed(Long retryId);
    
    /**
     * Convert JSON string back to variables map
     * 
     * @param jsonString JSON string
     * @return Variables map
     */
    Map<String, Object> convertJsonToVariables(String jsonString);
}