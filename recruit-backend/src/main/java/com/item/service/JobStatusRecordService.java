package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.JobStatusRecordEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 招聘状态流转记录 service
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  17:22
 */
public interface JobStatusRecordService extends IService<JobStatusRecordEntity> {

    /**
     * 保存记录
     * @param candidateJobId  申请者和职位id r_candidate_job表id
     * @param companyCode  公司编号
     * @param applyStatus  招聘流转状态
     * @param applyEvent  操作事件
     */
    public void saveJobStatusRecord(Long candidateJobId, String companyCode, Integer oldApplyStatus, Integer applyStatus, String applyEvent, LocalDateTime updateTime);

    /**
     * 根据申请者和职位id和操作事件查询记录
     * @param candidateJobId
     * @param applyEvent JobApplyStatusEvent
     * @return
     */
    public JobStatusRecordEntity getStatusRecordByEvent(Long candidateJobId,String applyEvent);

    /**
     * 根据申请者和职位id和操作事件批量查询记录
     * @param candidateJobIds
     * @param applyEvent  JobApplyStatusEvent
     * @return
     */
    public List<JobStatusRecordEntity> listByCandidateJobIdsAndEvent(List<Long> candidateJobIds,String applyEvent);

    public List<JobStatusRecordEntity> listByCandidateJobIdsAndApplyStatus(List<Long> candidateJobIds,Integer applyStatus);

    /**
     * 根据申请者和职位id查询记录
     * @param candidateJobId
     * @return
     */
    public List<JobStatusRecordEntity> listByCandidateJobId(Long candidateJobId);


    void deleteByCandidateJobId(Long candidateJobId);
}
