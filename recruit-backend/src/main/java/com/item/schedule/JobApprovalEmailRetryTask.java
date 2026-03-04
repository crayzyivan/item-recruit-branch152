package com.item.schedule;

import com.item.entity.JobApprovalEmailRetryEntity;
import com.item.framework.constant.JobApprovalConstants;
import com.item.service.JobApprovalEmailRetryService;
import com.item.task.core.handler.annotation.ScheduleTask;
import com.item.util.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Email retry scheduled task for job approval notifications
 * 
 * Retries failed email notifications every 30 minutes as per PRD requirements.
 * If retry fails, logs detailed failure info and notifies technical team.
 * 
 * @author system
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobApprovalEmailRetryTask {
    
    private final JobApprovalEmailRetryService emailRetryService;
    private final MailUtils mailUtils;
    
    /**
     * Retry failed email notifications every 30 minutes
     * 
     * This task:
     * 1. Finds all failed email attempts that need retry
     * 2. Attempts to resend each email
     * 3. If retry succeeds, marks as completed
     * 4. If retry fails again, logs detailed failure and notifies technical team
     */
    @ScheduleTask("jobApprovalEmailRetryHandler")
    public void retryFailedEmails() {
        log.info("Job approval email retry task started at: {}", LocalDateTime.now());
        
        try {
            // Get all failed email attempts that need retry
            List<JobApprovalEmailRetryEntity> failedEmails = emailRetryService.findEmailsToRetry();
            
            log.info("Found {} failed email attempts to retry", failedEmails.size());
            
            for (JobApprovalEmailRetryEntity failedEmail : failedEmails) {
                retryEmail(failedEmail);
            }
            
        } catch (Exception e) {
            log.error("Job approval email retry task failed with unexpected error", e);
        }
        
        log.info("Job approval email retry task completed at: {}", LocalDateTime.now());
    }
    
    /**
     * Retry a single failed email
     */
    private void retryEmail(JobApprovalEmailRetryEntity failedEmail) {
        try {
            // Convert JSON string back to Map
            Map<String, Object> variables = emailRetryService.convertJsonToVariables(failedEmail.getVariables());
            
            // Attempt to resend the email
            mailUtils.sendHtmlTemplateMail(
                failedEmail.getAdminEmail().split(","),
                JobApprovalConstants.EMAIL_SUBJECT_PREFIX + failedEmail.getAction(),
                failedEmail.getTemplateName(),
                variables
            );
            
            log.info("Email retry successful for job {} to {}", 
                failedEmail.getJobId(), failedEmail.getAdminEmail());
            
            // Mark as completed
            emailRetryService.markAsCompleted(failedEmail.getId());
            
        } catch (Exception e) {
            log.error("Email retry failed for job {} to {}: {}", 
                failedEmail.getJobId(), failedEmail.getAdminEmail(), e.getMessage());
            
            // Increment retry count
            failedEmail.setRetryCount(failedEmail.getRetryCount() + 1);
            emailRetryService.updateRetryCount(failedEmail.getId(), failedEmail.getRetryCount());
            
            // If still failing after retry, notify technical team
            if (failedEmail.getRetryCount() >= JobApprovalConstants.MAX_EMAIL_RETRY_COUNT) {
                notifyTechnicalTeam(failedEmail, e.getMessage());
                emailRetryService.markAsFailed(failedEmail.getId());
            }
        }
    }
    
    /**
     * Notify technical team about persistent email failures
     * 
     * As per PRD requirements: "a log is recorded (including position ID, recipient's email, and failure reason), and the technical team is notified"
     */
    private void notifyTechnicalTeam(JobApprovalEmailRetryEntity failedEmail, String failureReason) {
        // Log detailed failure info as per PRD requirements
        log.error("TECHNICAL TEAM NOTIFICATION - Persistent email failure: " +
            "Job ID: {}, Recipient: {}, Failure Reason: {}, Action: {}, " +
            "Retry Count: {}, First Failure Time: {}", 
            failedEmail.getJobId(), failedEmail.getAdminEmail(), failureReason, 
            failedEmail.getAction(), failedEmail.getRetryCount(), failedEmail.getFirstFailureTime());
    }
}
