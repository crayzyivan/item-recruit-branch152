package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.JobStatusRecordEntity;
import com.item.mapper.JobStatusRecordMapper;
import com.item.service.JobStatusRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 招聘状态流转记录
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  17:22
 */
@Service
public class JobStatusRecordServiceImpl extends ServiceImpl<JobStatusRecordMapper, JobStatusRecordEntity> implements JobStatusRecordService {

    /**
     * 保存记录
     * @param candidateJobId  申请者和职位id r_candidate_job表id
     * @param companyCode  公司编号
     * @param applyStatus  招聘流转状态
     * @param applyEvent  操作事件
     */
    @Override
    public void saveJobStatusRecord(Long candidateJobId, String companyCode,Integer oldApplyStatus,Integer applyStatus, String applyEvent,LocalDateTime updateTime) {
        JobStatusRecordEntity  jobStatusRecordEntity = new JobStatusRecordEntity();
        jobStatusRecordEntity.setCandidateJobId(candidateJobId);
        jobStatusRecordEntity.setCompanyCode(companyCode);
        jobStatusRecordEntity.setOldApplyStatus(oldApplyStatus);
        jobStatusRecordEntity.setApplyStatus(applyStatus);
        jobStatusRecordEntity.setApplyEvent(applyEvent);
        jobStatusRecordEntity.setCreateTime(updateTime);
        jobStatusRecordEntity.setUpdateTime(updateTime);
        save(jobStatusRecordEntity);
    }

    /**
     * 根据申请者和职位id和操作事件查询记录
     * @param candidateJobId
     * @param applyEvent JobApplyStatusEvent
     * @return
     */
    @Override
    public JobStatusRecordEntity getStatusRecordByEvent(Long candidateJobId, String applyEvent) {
        if (candidateJobId != null && applyEvent != null){
            LambdaQueryWrapper<JobStatusRecordEntity> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(JobStatusRecordEntity::getCandidateJobId,candidateJobId);
            wrapper.eq(JobStatusRecordEntity::getApplyEvent,applyEvent);
            wrapper.orderByDesc(JobStatusRecordEntity::getCreateTime);
            wrapper.last("LIMIT 1");
            List<JobStatusRecordEntity> list = this.list(wrapper);
            if (CollectionUtils.isNotEmpty(list)){
                return list.get(0);
            }
        }
        return null;
    }

    /**
     * 根据申请者和职位id和操作事件批量查询记录
     * @param candidateJobIds
     * @param applyEvent JobApplyStatusEvent
     * @return
     */
    @Override
    public List<JobStatusRecordEntity> listByCandidateJobIdsAndEvent(List<Long> candidateJobIds, String applyEvent) {
        if (CollectionUtils.isNotEmpty(candidateJobIds) && applyEvent != null){
            LambdaQueryWrapper<JobStatusRecordEntity> wrapper=new LambdaQueryWrapper<>();
            wrapper.in(JobStatusRecordEntity::getCandidateJobId,candidateJobIds);
            wrapper.eq(JobStatusRecordEntity::getApplyEvent,applyEvent);
            wrapper.orderByDesc(JobStatusRecordEntity::getCreateTime);
            return this.list(wrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public List<JobStatusRecordEntity> listByCandidateJobIdsAndApplyStatus(List<Long> candidateJobIds, Integer applyStatus) {
        if (CollectionUtils.isNotEmpty(candidateJobIds) && applyStatus != null){
            LambdaQueryWrapper<JobStatusRecordEntity> wrapper=new LambdaQueryWrapper<>();
            wrapper.in(JobStatusRecordEntity::getCandidateJobId,candidateJobIds);
            wrapper.eq(JobStatusRecordEntity::getApplyStatus,applyStatus);
            wrapper.orderByDesc(JobStatusRecordEntity::getCreateTime);
            return this.list(wrapper);
        }
        return new ArrayList<>();
    }

    /**
     * 根据申请者和职位id查询记录
     * @param candidateJobId
     * @return
     */
    @Override
    public List<JobStatusRecordEntity> listByCandidateJobId(Long candidateJobId) {
        if (candidateJobId==null){
            return new ArrayList<>();
        }
        LambdaUpdateWrapper<JobStatusRecordEntity> wrapper=new LambdaUpdateWrapper<>();
        wrapper.eq(JobStatusRecordEntity::getCandidateJobId,candidateJobId);
        wrapper.orderByAsc(JobStatusRecordEntity::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public void deleteByCandidateJobId(Long candidateJobId) {
        LambdaUpdateWrapper<JobStatusRecordEntity> wrapper=new LambdaUpdateWrapper<>();
        wrapper.eq(JobStatusRecordEntity::getCandidateJobId,candidateJobId);
        this.remove(wrapper);
    }
}