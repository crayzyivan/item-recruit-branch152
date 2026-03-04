package com.item.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.convert.AiCallbackConvert;
import com.item.entity.AiCallbackEntity;
import com.item.entity.JobEntity;
import com.item.mapper.AICallbackMapper;
import com.item.service.AIService;
import com.item.service.JobService;
import com.item.task.core.handler.annotation.ScheduleTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class InterviewResultTask {

    @Resource
    private AIService aiService;
    @Resource
    private AICallbackMapper aiCallbackMapper;
    @Resource
    private JobService jobService;

    @ScheduleTask("InterviewResultTaskHandler")
    public void InterviewResultTaskHandler() {
        LambdaQueryWrapper<AiCallbackEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(AiCallbackEntity::getStatus,0);
        List<AiCallbackEntity> aiCallbackEntities = aiCallbackMapper.selectList(queryWrapper);
        log.info("InterviewResultTaskHandler task start size:{}",aiCallbackEntities.size());
        if (CollectionUtils.isNotEmpty(aiCallbackEntities)){
            for (AiCallbackEntity aiCallbackEntity: aiCallbackEntities){
                try{
                    LambdaQueryWrapper<JobEntity> jobWrapper = new LambdaQueryWrapper<>();
                    jobWrapper.eq(JobEntity::getInterviewUrlId,aiCallbackEntity.getInterviewId());
                    JobEntity jobEntity = jobService.getOne(jobWrapper);
                    if (jobEntity == null){
                        return ;
                    }
                    aiService.getInterviewResult(AiCallbackConvert.INSTANCE.toAiCallbackDTO(aiCallbackEntity),jobEntity);
                }catch (Exception e){
                    log.error("InterviewResultTaskHandler error:",e);
                }
            }
        }
        log.info("InterviewResultTaskHandler task end ");
    }
}