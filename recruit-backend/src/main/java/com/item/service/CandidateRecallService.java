package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.CandidateRecallEntity;

import java.util.List;

/**
 * 候选人回拨服务接口
 */
public interface CandidateRecallService extends IService<CandidateRecallEntity> {

    /**
     * 查询所有记录，按创建时间倒序排列
     *
     * @return 候选人回拨记录列表
     */
    List<CandidateRecallEntity> listAllOrderByCreateTimeDesc();

    /**
     * 新增候选人回拨记录
     *
     * @param candidateRecallEntity 候选人回拨实体
     * @return 是否新增成功
     */
    boolean saveCandidateRecall(CandidateRecallEntity candidateRecallEntity);

    /**
     * 根据候选人职位ID删除记录
     *
     * @param candidateJobId 候选人职位ID
     * @return 是否删除成功
     */
    boolean deleteByCandidateJobId(Long candidateJobId);

    /**
     * 根据候选人职位ID更新短信状态
     *
     * @param candidateJobId 候选人职位ID
     * @param smsStatus 短信状态
     * @return 是否更新成功
     */
    boolean updateSmsStatusByCandidateJobId(Long candidateJobId, Integer smsStatus);

    /**
     * 查询短信状态为失败的记录
     *
     * @return 短信发送失败的候选人回拨记录列表
     */
    List<CandidateRecallEntity> listBySmsStatusFail();

    /**
     * 根据ID更新回拨状态
     *
     * @param id 候选人回拨记录ID
     * @param recallStatus 回拨状态
     * @return 是否更新成功
     */
    boolean updateRecallStatusById(Long id, Integer recallStatus);

    /**
     * 根据候选人职位ID查询记录
     *
     * @param candidateJobId 候选人职位ID
     * @return 候选人回拨记录
     */
    CandidateRecallEntity getByCandidateJobId(Long candidateJobId);

    /**
     * 根据短信状态和回拨状态查询记录
     *
     * @param smsStatus 短信状态
     * @param recallStatus 回拨状态
     * @return 候选人回拨记录列表
     */
    List<CandidateRecallEntity> listBySmsStatusAndRecallStatus(Integer smsStatus, Integer recallStatus);
}