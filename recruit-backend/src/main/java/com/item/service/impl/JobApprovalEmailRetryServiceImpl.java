package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.JobApprovalEmailRetryEntity;
import com.item.mapper.JobApprovalEmailRetryMapper;
import com.item.service.JobApprovalEmailRetryService;
import com.item.framework.constant.JobApprovalConstants;
import com.item.util.JsonUtils;
import com.item.util.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for handling job approval email retry operations
 * 
 * @author system
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobApprovalEmailRetryServiceImpl extends ServiceImpl<JobApprovalEmailRetryMapper, JobApprovalEmailRetryEntity> 
        implements JobApprovalEmailRetryService {
    
    private final MailUtils mailUtils;
    
    @Override
    public void sendEmailWithRetry(Long jobId, String action, String comment, 
                                   String templateName, Map<String, Object> variables,
                                   String[] recipient) {
        try {
            mailUtils.sendHtmlTemplateMail(recipient,
                JobApprovalConstants.EMAIL_SUBJECT_PREFIX + action, 
                templateName, variables);
            
            log.info("Email sent successfully for job {} to {}", jobId, Arrays.toString(recipient));
            
        } catch (Exception e) {
            log.error("Email sending failed for job {} to {}: {}", jobId, Arrays.toString(recipient), e.getMessage());
            
            // Store failed attempt for retry
            storeFailedEmailAttempt(jobId, action, comment, templateName, variables, recipient, e.getMessage());
            
            // Log detailed failure info as per PRD
            log.error("Email failure details - Job ID: {}, Recipient: {}, Failure Reason: {}, Action: {}", 
                jobId, Arrays.toString(recipient), e.getMessage(), action);
        }
    }
    
    /**
     * Store failed email attempt for retry
     */
    private void storeFailedEmailAttempt(Long jobId, String action, String comment, 
                                        String templateName, Map<String, Object> variables, 
                                        String[] recipient, String failureReason) {
        try {
            JobApprovalEmailRetryEntity retryEntity = new JobApprovalEmailRetryEntity();
            retryEntity.setJobId(jobId);
            retryEntity.setAction(action);
            retryEntity.setComment(comment);
            retryEntity.setTemplateName(templateName);
            retryEntity.setVariables(convertVariablesToJson(variables));
            retryEntity.setAdminEmail(String.join(",", recipient));
            retryEntity.setRetryCount(0);
            retryEntity.setStatus(JobApprovalConstants.EMAIL_RETRY_STATUS_PENDING);
            retryEntity.setFirstFailureTime(LocalDateTime.now());
            retryEntity.setLastRetryTime(LocalDateTime.now());
            retryEntity.setDeleted(false);
            retryEntity.setCreateTime(LocalDateTime.now());
            retryEntity.setUpdateTime(LocalDateTime.now());
            
            save(retryEntity);
            log.info("Stored failed email attempt for job {} to {} for retry", jobId, Arrays.toString(recipient));
            
        } catch (Exception e) {
            log.error("Failed to store email retry attempt for job {}: {}", jobId, e.getMessage());
        }
    }
    
    @Override
    public List<JobApprovalEmailRetryEntity> findEmailsToRetry() {
        LambdaQueryWrapper<JobApprovalEmailRetryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApprovalEmailRetryEntity::getStatus, JobApprovalConstants.EMAIL_RETRY_STATUS_PENDING)
               .lt(JobApprovalEmailRetryEntity::getRetryCount, JobApprovalConstants.MAX_EMAIL_RETRY_COUNT)
               .lt(JobApprovalEmailRetryEntity::getLastRetryTime, LocalDateTime.now().minusMinutes(JobApprovalConstants.EMAIL_RETRY_INTERVAL_MINUTES))
               .eq(JobApprovalEmailRetryEntity::getDeleted, false)
               .orderByAsc(JobApprovalEmailRetryEntity::getFirstFailureTime);
        
        return list(wrapper);
    }
    
    @Override
    public void markAsCompleted(Long retryId) {
        LambdaUpdateWrapper<JobApprovalEmailRetryEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(JobApprovalEmailRetryEntity::getId, retryId)
               .set(JobApprovalEmailRetryEntity::getStatus, JobApprovalConstants.EMAIL_RETRY_STATUS_COMPLETED)
               .set(JobApprovalEmailRetryEntity::getCompletedTime, LocalDateTime.now())
               .set(JobApprovalEmailRetryEntity::getUpdateTime, LocalDateTime.now());
        
        update(wrapper);
        log.info("Marked email retry {} as completed", retryId);
    }
    
    @Override
    public void updateRetryCount(Long retryId, Integer retryCount) {
        LambdaUpdateWrapper<JobApprovalEmailRetryEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(JobApprovalEmailRetryEntity::getId, retryId)
               .set(JobApprovalEmailRetryEntity::getRetryCount, retryCount)
               .set(JobApprovalEmailRetryEntity::getLastRetryTime, LocalDateTime.now())
               .set(JobApprovalEmailRetryEntity::getUpdateTime, LocalDateTime.now());
        
        update(wrapper);
        log.info("Updated retry count for email retry {} to {}", retryId, retryCount);
    }
    
    @Override
    public void markAsFailed(Long retryId) {
        LambdaUpdateWrapper<JobApprovalEmailRetryEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(JobApprovalEmailRetryEntity::getId, retryId)
               .set(JobApprovalEmailRetryEntity::getStatus, JobApprovalConstants.EMAIL_RETRY_STATUS_FAILED)
               .set(JobApprovalEmailRetryEntity::getUpdateTime, LocalDateTime.now());
        
        update(wrapper);
        log.info("Marked email retry {} as failed", retryId);
    }
    
    /**
     * Convert variables map to JSON string
     */
    private String convertVariablesToJson(Map<String, Object> variables) {
        try {
            return JsonUtils.toJson(variables);
        } catch (Exception e) {
            log.error("Failed to convert variables to JSON: {}", e.getMessage());
            return "{}";
        }
    }
    
    @Override
    public Map<String, Object> convertJsonToVariables(String jsonString) {
        try {
            return JsonUtils.toObject(jsonString, Map.class);
        } catch (Exception e) {
            log.error("Failed to convert JSON to variables map: {}", e.getMessage());
            return Map.of();
        }
    }
}
