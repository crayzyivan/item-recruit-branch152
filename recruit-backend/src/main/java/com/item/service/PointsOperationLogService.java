package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.PointsOperationLog;
import com.item.framework.http.Pager;
import com.item.vo.PointLogListVo;
import com.item.vo.PointLogQueryVo;

import java.util.List;

/**
 * 积分操作记录
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  09:41
 */
public interface PointsOperationLogService extends IService<PointsOperationLog> {

    /**
     * 查询ai面试冻结记录
     *
     * @param candidateJobId 候选人与职位关联id
     * @return 冻结记录
     */
    PointsOperationLog selectinterviewFreezeLog(Long candidateJobId);

    /**
     * 查询ai筛选记录
     * @param candidateJobId
     * @return
     */
    PointsOperationLog selectScreenPointsOperationLog(Long candidateJobId);

    /**
     * 查询积分冻结记录
     *
     * @param transactionNo 交易号
     * @param transactionType 交易类型
     * @return 冻结记录
     */
    PointsOperationLog selectFreezeLogByNo(Long userId,String transactionNo, Integer transactionType);

    /**
     * 查询需要重试的待确认或取消的积分冻结记录
     * @return
     */
    List<PointsOperationLog> listByConfirmOrCancelRetryLogs();

    /**
     * 积分操作记录分页列表
     *
     * @param queryVo 查询参数
     * @return 积分操作记录分页列表
     */
    Pager<PointLogListVo> selectPointLogPageList(PointLogQueryVo queryVo);


    /**
     * 根据职位查询音频面试冻结记录
     * @param jobId
     * @return
     */
    List<PointsOperationLog> audioFreezeListByJobId(Long jobId);
}
