package com.item.service.impl;

import com.item.entity.CandidateJobEntity;
import com.item.es.ResumeEsService;
import com.item.framework.constant.CommonConstants;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.service.CandidateJobService;
import com.item.service.JobFlowService;
import com.item.service.JobStatusRecordService;
import com.item.util.LambdaUtil;
import com.item.vo.ai.JobMatchResultVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 招聘状态流转
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  16:35
 */
@Slf4j
@Service
public class JobFlowServiceImpl implements JobFlowService {

    @Resource
    private StateMachineFactory<JobApplyStatus, JobApplyStatusEvent> stateMachineFactory;
    @Resource
    private CandidateJobService candidateJobService;
    @Resource
    private JobStatusRecordService jobStatusRecordService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ResumeEsService resumeEsService;


    /**
     * 更新招聘流程状态
     * @param candidateJobId  候选人职位关联表id
     * @param jobApplyStatusEvent 更新招聘状态事件
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fireEvent(Long candidateJobId, JobApplyStatusEvent jobApplyStatusEvent) {
        if(candidateJobId==null || jobApplyStatusEvent==null){
            log.warn("Invalid parameters: candidateJobId={}, event={}", candidateJobId, jobApplyStatusEvent);
            return;
        }
        log.info("Fire event: candidateJobId={}, event={}", candidateJobId, jobApplyStatusEvent);
        RLock lock = redissonClient.getLock(CommonConstants.JOB_STATUS_LOCK_PREFIX + candidateJobId);
        boolean locked = false;
        try{
            locked = lock.tryLock();
            if (!locked){
                throw new RuntimeException(CommonConstants.EXCEPTION_SYSTEM_IS_BUSY);
            }
            //1 获取当前招聘状态
            CandidateJobEntity candidateJobEntity = candidateJobService.getById(candidateJobId);
            if (candidateJobEntity!=null){
                Integer applyStatus = candidateJobEntity.getApplyStatus();
                log.info("Fire event: candidateJobId={}, applyStatus={}", candidateJobId, applyStatus);
                JobApplyStatus oldApplyStatus = JobApplyStatus.fromCode(applyStatus);
                //判断是否已收到ai面试结果
                if (applyStatus==JobApplyStatus.VETTED.getCode() && jobApplyStatusEvent==JobApplyStatusEvent.VETTED){
                    log.info("The candidate has already received the ai interview result:{}",candidateJobId);
                    return;
                }
                //2 执行状态流转
                JobApplyStatus newApplyStatus=transitionStatus(oldApplyStatus,jobApplyStatusEvent);
                log.info("Fire event: candidateJobId={}, newApplyStatus={}", candidateJobId, newApplyStatus);
                if (oldApplyStatus.getCode()!=newApplyStatus.getCode()){
                    //3 更新招聘状态
                    candidateJobEntity.setApplyStatus(newApplyStatus.getCode());
                    LocalDateTime updateTime = LocalDateTime.now();
                    //处理mysql与es时间不一致问题
                    updateTime = updateTime.truncatedTo(ChronoUnit.SECONDS);
                    candidateJobEntity.setUpdateTime(updateTime);
                    candidateJobService.updateById(candidateJobEntity);
                    //4 状态流转记录
                    jobStatusRecordService.saveJobStatusRecord(candidateJobEntity.getId(),candidateJobEntity.getCompanyCode(),oldApplyStatus.getCode(),newApplyStatus.getCode(),jobApplyStatusEvent.toString(),updateTime);
                    Map<String,Object> fieldsValues = new HashMap<>();
                    fieldsValues.put(LambdaUtil.getFieldName(JobMatchResultVO::getApplyStatus),newApplyStatus.getCode());
                    fieldsValues.put(LambdaUtil.getFieldName(JobMatchResultVO::getApplyStatusName),JobApplyStatus.fromCode(newApplyStatus.getCode()).getName());
                    fieldsValues.put(LambdaUtil.getFieldName(JobMatchResultVO::getUpdateTime), updateTime);
                    resumeEsService.updateMatchEsFieldValue(String.valueOf(candidateJobEntity.getId()),fieldsValues);
                }
            }
        } catch (Exception e) {
            log.error("State transition error: candidateJobId={}, event={}", candidateJobId, jobApplyStatusEvent, e);
            throw e;
        }finally {
            if (locked){
                lock.unlock();
            }
        }
    }

    /**
     * 执行状态流转、获取状态机执行事件后状态
     * @param currentStatus
     * @param event
     * @return
     */
    private JobApplyStatus transitionStatus(JobApplyStatus currentStatus, JobApplyStatusEvent event) {
        StateMachine<JobApplyStatus, JobApplyStatusEvent> stateMachine = stateMachineFactory.getStateMachine();
        try {
            stateMachine.start();
            stateMachine.getStateMachineAccessor().doWithAllRegions(accessor ->
                    accessor.resetStateMachine(new DefaultStateMachineContext<>(currentStatus, null, null, null)));

            if (!stateMachine.sendEvent(event)) {
                log.error("Invalid transition from {} with event {}", currentStatus, event);
                throw new RuntimeException(CommonConstants.EXCEPTION_INVALID_TRANSITION_EVENT);
            }
            return stateMachine.getState().getId();
        } finally {
            stateMachine.stop();
        }
    }

}