package com.item.schedule;

import com.item.entity.PointsOperationLog;
import com.item.framework.constant.FreezeActionTypeEnum;
import com.item.service.PointService;
import com.item.service.PointsOperationLogService;
import com.item.task.core.handler.annotation.ScheduleTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 积分取消占用、确认占用失败重试任务
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-31  11:08
 */
@Component
@Slf4j
public class PointRetryTask {

    @Resource
    private PointsOperationLogService pointsOperationLogService;
    @Resource
    private PointService pointService;


    @ScheduleTask("pointRetryHandler")
    public void cancelInterviewFreezeTask() {
        List<PointsOperationLog> logs= pointsOperationLogService.listByConfirmOrCancelRetryLogs();
        log.info("pointRetryHandler task start size:{}",logs.size());
        if (CollectionUtils.isNotEmpty(logs)){
            for (PointsOperationLog operationLog:logs){
                try{
                    log.info("pointRetryHandler task operationLog id:{}",operationLog.getId());
                    if (operationLog.getFreezeActionType()!=null && operationLog.getFreezeActionType()== FreezeActionTypeEnum.CAMCEL.getCode()){
                        pointService.cancelPointsFreezeByLog(operationLog);
                    }else if (operationLog.getFreezeActionType()!=null && operationLog.getFreezeActionType()== FreezeActionTypeEnum.CONFIRM.getCode()){
                        pointService.confirmPointsFreezeByLog(operationLog);
                    }
                }catch (Exception e){
                    log.error("pointRetryHandler task error",e);
                }
            }
        }
        log.info("pointRetryHandler task end");
    }
}