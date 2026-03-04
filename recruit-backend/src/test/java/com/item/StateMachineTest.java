package com.item;

import com.item.entity.CandidateJobEntity;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.service.CandidateJobService;
import com.item.service.JobFlowService;
import com.item.service.JobStatusRecordService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

/**
 * 测试招聘状态
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  18:18
 */
@SpringBootTest
public class StateMachineTest {

    @Resource
    private JobFlowService jobFlowService;
    @Resource
    private CandidateJobService candidateJobService;
    @Resource
    private JobStatusRecordService jobStatusRecordService;

    /**
     * 招聘状态初始化
     */
    @Test
    public void init(){
        CandidateJobEntity  candidateJobEntity = new CandidateJobEntity();
        candidateJobEntity.setCandidateId(5296L);
        candidateJobEntity.setJobId(206L);
        candidateJobEntity.setCompanyCode("001");
        candidateJobEntity.setApplyStatus(JobApplyStatus.SUBMITTED.getCode());
        candidateJobEntity.setCustomerId(1L);
        LocalDateTime dateTime=LocalDateTime.now();
        candidateJobEntity.setCreateTime(dateTime);
        candidateJobService.applyJob(candidateJobEntity);
        jobStatusRecordService.saveJobStatusRecord(candidateJobEntity.getId(),candidateJobEntity.getCompanyCode(),null,candidateJobEntity.getApplyStatus(), JobApplyStatusEvent.SUBMIT.toString(),dateTime);
    }

    /**
     * 招聘状态流转
     */
    @Test
    public void testStatusProcess(){
        Long candidateId=791L;
//        JobApplyStatusEvent screenEvent=JobApplyStatusEvent.SCREEN;
//        jobFlowService.fireEvent(candidateId,screenEvent);
//        JobApplyStatusEvent eventVet=JobApplyStatusEvent.VETTED;
//        jobFlowService.fireEvent(candidateId,eventVet);
//        JobApplyStatusEvent aiPassEvent=JobApplyStatusEvent.AI_PASS;
//        jobFlowService.fireEvent(candidateId,aiPassEvent);
//        JobApplyStatusEvent reviewPassEvent=JobApplyStatusEvent.REVIEW_PASS;
//        jobFlowService.fireEvent(candidateId,reviewPassEvent);
//        JobApplyStatusEvent backgroundEvent=JobApplyStatusEvent.BACKGROUND_CHECK;
//        jobFlowService.fireEvent(candidateId,backgroundEvent);
//        JobApplyStatusEvent rejectEvent=JobApplyStatusEvent.REJECT;
//        jobFlowService.fireEvent(candidateId,rejectEvent);

//        JobApplyStatusEvent eventVet=JobApplyStatusEvent.AUTO_AI_TO_REVIEW;
//        jobFlowService.fireEvent(candidateId,eventVet);

        JobApplyStatusEvent eventVet=JobApplyStatusEvent.AUTO_REVIEW_PASS;
        jobFlowService.fireEvent(candidateId,eventVet);

    }

}