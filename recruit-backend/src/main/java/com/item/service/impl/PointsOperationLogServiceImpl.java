package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.convert.PointOperationLogConverter;
import com.item.entity.CandidateJobEntity;
import com.item.entity.PointsOperationLog;
import com.item.framework.constant.InterviewTypeEnum;
import com.item.framework.constant.PointStatusEnum;
import com.item.framework.constant.TransactionTypeEnum;
import com.item.framework.http.Pager;
import com.item.mapper.PointsOperationLogMapper;
import com.item.service.PointsOperationLogService;
import com.item.vo.PointLogListVo;
import com.item.vo.PointLogQueryVo;
import com.item.vo.ReadyListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 积分操作记录
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  09:41
 */
@Service
public class PointsOperationLogServiceImpl extends ServiceImpl<PointsOperationLogMapper, PointsOperationLog> implements PointsOperationLogService {

    /**
     * 查询ai面试冻结记录
     *
     * @param candidateJobId 候选人与职位关联id
     * @return 积分记录
     */
    @Override
    public PointsOperationLog selectinterviewFreezeLog(Long candidateJobId) {
        if (candidateJobId==null){
            return null;
        }
        LambdaQueryWrapper<PointsOperationLog> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(PointsOperationLog::getCandidateJobId,candidateJobId);
        wrapper.eq(PointsOperationLog::getTransactionType, TransactionTypeEnum.AI_INTERVIEW.getCode());
        wrapper.eq(PointsOperationLog::getPointStatus, PointStatusEnum.FREEZE.getCode());
        List<PointsOperationLog> list = this.list(wrapper);
        if (CollectionUtils.isNotEmpty( list)){
            return list.getFirst();
        }
        return null;
    }

    @Override
    public PointsOperationLog selectScreenPointsOperationLog(Long candidateJobId) {
        if (candidateJobId==null){
            return null;
        }
        LambdaQueryWrapper<PointsOperationLog> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(PointsOperationLog::getCandidateJobId,candidateJobId);
        wrapper.eq(PointsOperationLog::getTransactionType, TransactionTypeEnum.RESUME_SCREEN.getCode());
        wrapper.ne(PointsOperationLog::getPointStatus, PointStatusEnum.ERROR.getCode());
        wrapper.last("limit 1");
        return this.getOne(wrapper);
    }

    /**
     * 查询积分冻结记录
     *
     * @param transactionNo 交易号
     * @param transactionType 交易类型
     * @return 冻结记录
     */
    @Override
    public PointsOperationLog selectFreezeLogByNo(Long userId,String transactionNo, Integer transactionType) {
        LambdaQueryWrapper<PointsOperationLog> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(PointsOperationLog::getUserId,userId);
        wrapper.eq(PointsOperationLog::getTransactionNo,transactionNo);
        wrapper.eq(PointsOperationLog::getTransactionType, transactionType);
        wrapper.eq(PointsOperationLog::getPointStatus, PointStatusEnum.FREEZE.getCode());
        List<PointsOperationLog> list = this.list(wrapper);
        if (CollectionUtils.isNotEmpty( list)){
            return list.getFirst();
        }
        return null;
    }

    /**
     * 查询需要重试的待确认或取消的积分冻结记录
     * @return
     */
    @Override
    public List<PointsOperationLog> listByConfirmOrCancelRetryLogs() {
        LambdaQueryWrapper<PointsOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsOperationLog::getPointStatus, PointStatusEnum.FREEZE.getCode());
        wrapper.and(w -> w.isNull(PointsOperationLog::getErrorMessage)
                .or()
                .eq(PointsOperationLog::getErrorMessage, ""));
        return this.list(wrapper);
    }

    /**
     * 积分操作记录分页列表
     *
     * @param queryVo 查询参数
     * @return 积分操作记录分页列表
     */
    @Override
    public Pager<PointLogListVo> selectPointLogPageList(PointLogQueryVo queryVo) {
        Page<PointsOperationLog> page = new Page<>(queryVo.getPageIndex(), queryVo.getPageSize());
        LambdaQueryWrapper<PointsOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        if(Objects.nonNull(queryVo.getTransactionNo())) {
            queryWrapper.like(PointsOperationLog::getTransactionNo, queryVo.getTransactionNo());
        }
        if(Objects.nonNull(queryVo.getPointStatus())) {
            queryWrapper.eq(PointsOperationLog::getPointStatus, queryVo.getPointStatus());
        }
        if(Objects.nonNull(queryVo.getTransactionType())) {
            queryWrapper.eq(PointsOperationLog::getTransactionType, queryVo.getTransactionType());
        }
        if(Objects.nonNull(queryVo.getUserId())) {
            queryWrapper.eq(PointsOperationLog::getUserId, queryVo.getUserId());
        }
        queryWrapper.orderByDesc(PointsOperationLog::getCreateTime);
        page(page, queryWrapper);

        List<PointsOperationLog> records = page.getRecords();
        List<PointLogListVo> logListVos = PointOperationLogConverter.INSTANCE.convertListToListVo(records);

        Pager<PointLogListVo> pager = new Pager<>();
        pager.setCurrentPageRecords(logListVos);
        pager.setPageIndex(queryVo.getPageIndex());
        pager.setPageSize(queryVo.getPageSize());
        pager.setTotalCount(page.getTotal());
        return pager;
    }


    /**
     * 根据职位查询音频面试冻结记录
     * @param jobId
     * @return
     */
    @Override
    public List<PointsOperationLog> audioFreezeListByJobId(Long jobId) {
        LambdaQueryWrapper<PointsOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsOperationLog::getTransactionType, TransactionTypeEnum.AI_INTERVIEW.getCode());
        wrapper.eq(PointsOperationLog::getPointStatus, PointStatusEnum.FREEZE.getCode());
        wrapper.eq(PointsOperationLog::getFreezeInterviewType, InterviewTypeEnum.AUDIO.getCode());
        wrapper.eq(PointsOperationLog::getJobId,jobId);
        return this.list(wrapper);
    }
}
