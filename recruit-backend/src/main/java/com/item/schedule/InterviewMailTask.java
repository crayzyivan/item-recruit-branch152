package com.item.schedule;

import com.item.dto.job.IntelligenceScoreRuleDTO;
import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.AiInterviewConfig;
import com.item.framework.config.BusinessDeductionPointsConfig;
import com.item.framework.config.MinuteBasedConfig;
import com.item.framework.constant.*;
import com.item.framework.error.BusinessException;
import com.item.service.CandidateJobDomainService;
import com.item.service.CandidateJobService;
import com.item.service.JobService;
import com.item.service.PointService;
import com.item.service.migration.DataMigrationMappingService;
import com.item.task.core.handler.annotation.ScheduleTask;
import com.item.util.CommonUtils;
import com.item.util.RedisKeyUtil;
import com.item.util.RedisSerialNumberUtils;
import com.item.vo.PointLogVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 面试邮件发送定时任务
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-26  10:51
 */
@Component
@Slf4j
public class InterviewMailTask {

    @Resource
    private CandidateJobService candidateJobService;
    
    @Resource
    private CandidateJobDomainService candidateJobDomainService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private DataMigrationMappingService dataMigrationMappingService;
    @Resource
    private AiInterviewConfig aiInterviewConfig;
    @Resource
    private JobService jobService;
    @Resource
    private JobEsService jobEsService;

    /**
     * 面试邮件发送定时任务主方法
     */
    @ScheduleTask("interviewMail")
    public void interviewMail() {
        LocalDateTime cutoffTime = LocalDateTime.now(ZoneOffset.UTC).minusDays(aiInterviewConfig.getInterviewMailBeforeDay());
        List<CandidateJobEntity> candidateJobEntities = candidateJobService.listNotSentInterviewMailCandidates(cutoffTime);
        log.info("Interview mail task started, candidate count: {}", candidateJobEntities.size());
        
        if (CollectionUtils.isEmpty(candidateJobEntities)) {
            log.info("No candidates found for interview mail sending");
            return;
        }

        // 批量获取 JobEsEntity 并构建 Map
        List<Long> jobIds = candidateJobEntities.stream()
                .map(CandidateJobEntity::getJobId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, JobEsEntity> jobEsMap = jobEsService.getJobByIds(jobIds).stream()
                .collect(Collectors.toMap(JobEsEntity::getId, job -> job));
        
        log.info("Fetched {} jobs for {} candidates", jobEsMap.size(), candidateJobEntities.size());

        int successCount = 0;
        int failureCount = 0;

        for (CandidateJobEntity candidateJob : candidateJobEntities) {

            JobEsEntity job = jobEsMap.get(candidateJob.getJobId());
            if (job == null) {
                log.error("Job not found for jobId: {}", candidateJob.getJobId());
                continue;
            }

            if (job.getIntelligenceSwitch()) {
                IntelligenceScoreRuleDTO assessmentRule = job.getScoreRules().stream()
                        .filter(rule -> JobIntelligenceSubStageEnum.ASSESSMENT_SCORE.getCode().equals(rule.getSubStageCode()))
                        .findFirst()
                        .orElse(null);
                
                if (assessmentRule != null && candidateJob.getAssessmentScore() < assessmentRule.getThreshold()) {
                    log.info("CandidateJobId: {} does not meet the intelligence rule threshold (score: {}, required: {}), skipping", 
                            candidateJob.getId(), candidateJob.getAssessmentScore(), assessmentRule.getThreshold());
                    continue;
                }
            } else {
                if (CommonConstants.MIN_INTERVIEW_MAIL_SCORE > candidateJob.getAssessmentScore()) {
                    log.info("CandidateJobId: {} does not meet the minimum interview mail score requirement, skipping", candidateJob.getId());
                    continue;
                }
            }

            RLock lock = redissonClient.getLock(RedisKeyUtil.getLockSendInterviewUrl(candidateJob.getId()));
            boolean locked = false;
            try {
                locked = lock.tryLock();
                if (locked) {
                    //菲律宾同步数据不自动发送邮件
                    Optional<DataMigrationMappingEntity> mappingEntityOptional = dataMigrationMappingService.findByMysqlIdAndType(candidateJob.getId(), MigrationBusTypeEnum.CANDIDATE_JOB);
                    if (mappingEntityOptional.isPresent()){
                        return;
                    }
                    candidateJobDomainService.processInterviewMail(candidateJob,null,true);
                    successCount++;
                    log.info("Successfully processed interview mail for candidateJobId: {}", candidateJob.getId());
                }
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to process interview mail for candidateJobId: {}, error: {}", 
                         candidateJob.getId(), e.getMessage(), e);
            }finally {
                if (locked) {
                    lock.unlock();
                }
            }
        }
        
        log.info("Interview mail task completed. Success: {}, Failure: {}", successCount, failureCount);
    }
}