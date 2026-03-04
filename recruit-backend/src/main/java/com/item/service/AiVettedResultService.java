package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.AiVettedResultEntity;

import java.util.List;

/**
 * AI审核结果表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:18
 */
public interface AiVettedResultService extends IService<AiVettedResultEntity> {

    /**
     * 根据职位申请ID查询
     * @param candidateJobId
     * @return
     */
    AiVettedResultEntity selectByCandidateJobId(Long candidateJobId);

    /**
     * 根据职位申请ID列表查询
     * @param candidateJobIds
     * @return
     */
    List<AiVettedResultEntity> listByCandidateJobIds(List<Long> candidateJobIds);



}