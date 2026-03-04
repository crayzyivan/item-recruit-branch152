package com.item.schedule;

import com.item.convert.JobConvert;
import com.item.dto.job.JobCreateBO;
import com.item.entity.JobEntity;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.service.JobDomainService;
import com.item.service.JobPostProcessingService;
import com.item.service.JobService;
import com.item.task.core.handler.annotation.ScheduleTask;
import com.item.vo.ai.InterviewResultVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 生成ai面试url id
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-25  19:11
 */
@Slf4j
@Component
public class InterviewUrlIdTask {

    @Resource
    private JobService jobService;
    @Resource
    private JobEsService jobEsService;
    @Resource
    private JobDomainService jobDomainService;
    @Resource
    private JobPostProcessingService jobPostProcessingService;


    @ScheduleTask("interviewUrlIdHandler")
    public void interviewUrlIdTaskHandler() {
        List<Long> jobIds = jobService.selectNotCreateInterviewUrlId();
        log.info("interviewUrlIdHandler task start size:{}",jobIds.size());
        if (CollectionUtils.isNotEmpty(jobIds)){
            for (Long jobId: jobIds){
                try{
                    JobEsEntity jobEsEntity = jobEsService.getJobById(jobId);
                    if (jobEsEntity!=null){
                        log.info("interviewUrlIdHandler start jobId:{}",jobId);
                        //获取ai面试url id
                        JobCreateBO jobCreateBO = JobConvert.INSTANCE.toJobCreateBoFromEntity(jobEsEntity);
                        InterviewResultVO interviewResultVO = jobPostProcessingService.createAIInterview(jobCreateBO);
                        log.info("interviewUrlIdHandler end jobId:{},urlId:{}",jobId,interviewResultVO.getUrlId());
                        //更新面试url id
                        JobEntity job = jobService.getById(jobId);
                        if (StringUtils.isEmpty(job.getInterviewUrlId())){
                            job.setInterviewUrlId(interviewResultVO.getUrlId());
                            jobService.updateById( job);
                        }
                    }
                }catch (Exception e){
                    log.error("interviewUrlIdHandler error:",e);
                }
            }
        }
        log.info("interviewUrlIdHandler task end ");
    }
}
