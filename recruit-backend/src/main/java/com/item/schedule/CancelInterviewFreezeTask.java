package com.item.schedule;

import com.item.entity.PointsOperationLog;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.service.CandidateJobService;
import com.item.service.JobFlowService;
import com.item.service.PointService;
import com.item.service.PointsOperationLogService;
import com.item.task.core.handler.annotation.ScheduleTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 面试取消时冻结积分解冻
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-30  17:29
 */
@Component
@Slf4j
public class CancelInterviewFreezeTask {

    @Resource
    private CandidateJobService candidateJobService;
    @Resource
    private PointsOperationLogService pointsOperationLogService;
    @Resource
    private PointService pointService;
    @Resource
    private JobFlowService jobFlowService;

    @ScheduleTask("cancelInterviewFreezeHandler")
    public void cancelInterviewFreezeTask() {
        List<Long> candidateJobIds=candidateJobService.getCancelInterviewFreezeIds();
        log.info("cancelInterviewFreeze task start size:{}",candidateJobIds.size());
        if (CollectionUtils.isNotEmpty(candidateJobIds)){
            for (Long candidateJobId:candidateJobIds){
                log.info("cancelInterviewFreeze task candidateJobId:{}",candidateJobId);
                jobFlowService.fireEvent(candidateJobId, JobApplyStatusEvent.REJECT);
                PointsOperationLog pointsOperationLog = pointsOperationLogService.selectinterviewFreezeLog(candidateJobId);
                if (pointsOperationLog!=null){
                    log.info("cancelInterviewFreeze task pointsOperationLog:{}",pointsOperationLog);
                    Boolean b = pointService.cancelPointsFreezeByLog(pointsOperationLog);
                    log.info("cancelInterviewFreeze task result:{}",b);
                }
            }
        }
        log.info("cancelInterviewFreeze task end");
    }


}