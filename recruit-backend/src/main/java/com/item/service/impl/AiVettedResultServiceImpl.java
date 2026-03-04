package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.AiVettedResultEntity;
import com.item.mapper.AiVettedResultMapper;
import com.item.service.AiVettedResultService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI审核结果表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:18
 */
@Service
public class AiVettedResultServiceImpl extends ServiceImpl<AiVettedResultMapper, AiVettedResultEntity> implements AiVettedResultService{

    /**
     * 根据职位申请ID查询
     * @param candidateJobId
     * @return
     */
    @Override
    public AiVettedResultEntity selectByCandidateJobId(Long candidateJobId) {
        if (candidateJobId!=null){
            LambdaQueryWrapper<AiVettedResultEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiVettedResultEntity::getCandidateJobId, candidateJobId);
            wrapper.orderByDesc(AiVettedResultEntity::getCandidateJobId);
            wrapper.last("LIMIT 1");
            List<AiVettedResultEntity> list = this.list(wrapper);
            if (CollectionUtils.isNotEmpty(list)){
                return list.get(0);
            }
        }
        return null;
    }

    /**
     * 根据职位申请ID列表查询
     * @param candidateJobIds
     * @return
     */
    @Override
    public List<AiVettedResultEntity> listByCandidateJobIds(List<Long> candidateJobIds) {
        if (CollectionUtils.isNotEmpty(candidateJobIds)){
            LambdaQueryWrapper<AiVettedResultEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(AiVettedResultEntity::getCandidateJobId, candidateJobIds);
            wrapper.orderByDesc(AiVettedResultEntity::getCandidateJobId);
            return this.list(wrapper);
        }
        return new ArrayList<>();
    }
}