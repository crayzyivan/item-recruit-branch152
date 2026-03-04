package com.item.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.item.entity.CandidateJobEntity;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.service.CandidateJobService;
import com.item.service.JobFlowService;
import com.item.service.JobStatusRecordService;
import com.item.task.core.handler.annotation.ScheduleTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 候选人投递记录冷却期定时任务
 * 
 * 根据配置的冷却时间(repeatApplyJobLimitDay)，将r_candidate_job表中
 * 更新时间距今大于冷却时间且不是拒绝状态的记录更新为拒绝状态
 * 
 * 对应Jira任务: RP-226
 * 
 * @author yunlong.li
 * @since 2025-09-16
 */
@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class CandidateJobCooldownTask {

    private final CandidateJobService candidateJobService;
    private final JobFlowService jobFlowService;

    /**
     * 冷却时间配置，单位：天
     * 默认值：180天
     */
    @Value("${repeat.apply.job.limit.day:180}")
    private int repeatApplyJobLimitDay;


    /**
     * 冷却期候选人投递记录拒绝定时任务
     * 
     * 执行逻辑：
     * 1. 检查是否启用冷却期功能
     * 2. 查询满足冷却期条件的候选人投递记录
     * 3. 批量更新这些记录的状态为拒绝
     * 4. 记录状态变更日志
     */
    @ScheduleTask("candidateJobCooldownHandler")
    @Transactional(rollbackFor = Exception.class)
    public void candidateJobCooldownHandler() {
        log.info("Candidate job cooldown task started. Cooldown days: {}", repeatApplyJobLimitDay);

        try {
            // 计算冷却期截止时间
            LocalDateTime cooldownCutoffTime = LocalDateTime.now().minusDays(repeatApplyJobLimitDay);
            log.info("Cooldown cutoff time: {}", cooldownCutoffTime);

            // 查询满足冷却期条件的候选人投递记录
            List<CandidateJobEntity> expiredCandidateJobs = candidateJobService.findExpiredCandidateJobs(cooldownCutoffTime);
            
            if (CollectionUtils.isEmpty(expiredCandidateJobs)) {
                log.info("No expired candidate job records found");
                return;
            }
            log.info("Found {} expired candidate job records to update", expiredCandidateJobs.size());
            for (CandidateJobEntity candidateJob : expiredCandidateJobs){
                log.info("candidateJobCooldownHandler Updating candidate job record: {}", candidateJob.getId());
                jobFlowService.fireEvent(candidateJob.getId(), JobApplyStatusEvent.REJECT);
            }
            log.info("Candidate job cooldown task completed successfully. Updated {} records", expiredCandidateJobs.size());
        } catch (Exception e) {
            log.error("Candidate job cooldown task failed with error", e);
        }
    }


}
