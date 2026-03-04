package com.item.schedule;

import com.item.entity.CandidateJobEntity;
import com.item.service.CandidateJobDomainService;
import com.item.service.CandidateJobService;
import com.item.task.core.handler.annotation.ScheduleTask;
import com.item.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewPhoneTask {

    private final CandidateJobService candidateJobService;
    private final CandidateJobDomainService candidateJobDomainService;
    private final RedissonClient redissonClient;

    /**
     * 预约电话面试定时任务主方法，兜底方案
     */
    @ScheduleTask("bookInterviewPhone")
    public void bookInterviewPhone() {
        List<CandidateJobEntity> candidateJobEntities = candidateJobService.listNotBookInterviewPhoneCandidates();
        log.info("Interview mail task started, candidate count: {}", candidateJobEntities.size());

        if (CollectionUtils.isEmpty(candidateJobEntities)) {
            log.info("No candidates found for interview mail sending");
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (CandidateJobEntity candidateJob : candidateJobEntities) {
            RLock lock = redissonClient.getLock(RedisKeyUtil.getLockSendInterviewUrl(candidateJob.getId()));
            boolean locked = false;
            try {
                locked = lock.tryLock();
                if (locked) {
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
