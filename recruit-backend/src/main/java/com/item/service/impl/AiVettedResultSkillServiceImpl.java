package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.AiVettedResultSkillEntity;
import com.item.mapper.AiVettedResultSkillMapper;
import com.item.service.AiVettedResultSkillService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI审核技能明细表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:18
 */
@Service
public class AiVettedResultSkillServiceImpl extends ServiceImpl<AiVettedResultSkillMapper, AiVettedResultSkillEntity> implements AiVettedResultSkillService {

    /**
     * 根据 ai审核结果表id 获取技能明细
     *
     * @param vettedResultIds ai审核结果表id
     * @return 技能明细
     */
    @Override
    public List<AiVettedResultSkillEntity> listByVettedResultIds(List<Long> vettedResultIds) {
        if (CollectionUtils.isNotEmpty(vettedResultIds)){
            LambdaQueryWrapper<AiVettedResultSkillEntity> wrapper=new LambdaQueryWrapper<>();
            wrapper.in(AiVettedResultSkillEntity::getVettedResultId, vettedResultIds);
            wrapper.orderByDesc(AiVettedResultSkillEntity::getVettedResultId);
            return this.list(wrapper);
        }
        return new ArrayList<>();
    }

    /**
     * 删除
     * @param vettedResultId
     */
    @Override
    public void deleteByVettedResultId(Long vettedResultId) {
        LambdaUpdateWrapper<AiVettedResultSkillEntity>  wrapper=new LambdaUpdateWrapper<>();
        wrapper.eq(AiVettedResultSkillEntity::getVettedResultId,vettedResultId);
        this.remove(wrapper);
    }
}