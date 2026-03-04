package com.item.schedule;

import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.service.CandidateJobDomainService;
import com.item.service.CandidateJobService;
import com.item.service.JobCategoryService;
import com.item.service.JobService;
import com.item.task.core.handler.annotation.ScheduleTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ai简历匹配
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-26  09:47
 */
@Component
@Slf4j
public class ResumeAIMatchTask {

    @Resource
    private CandidateJobService candidateJobService;
    @Resource
    private CandidateJobDomainService candidateJobDomainService;
    @Resource
    private JobService jobService;


    @ScheduleTask("resumeAIMatchTaskHandler") 
    public void resumeAIMatchTaskHandler() {
        List<CandidateJobEntity> candidateJobEntities = candidateJobService.listByAssessmentScoreIsNull();
        log.info("resumeAIMatchTaskHandler start size:{}",candidateJobEntities.size());
        if (CollectionUtils.isNotEmpty(candidateJobEntities)){
            for (CandidateJobEntity candidateJob:candidateJobEntities){
                try {
                    log.info("resumeAIMatchTaskHandler resumeAIMatch candidateJobId:{}",candidateJob.getCandidateId());
                    JobEntity job = jobService.getById(candidateJob.getJobId());
                    if (job!=null){
                        candidateJobDomainService.resumeAIMatch(candidateJob,job.getCreateBy(),job.getCompanyCode());
                    }
                }catch (Exception e){
                    log.error("resumeAIMatchTaskHandler failed to resumeAIMatch for candidateJobId:{}", candidateJob.getCandidateId(), e);
                }
            }
        }
    }
}