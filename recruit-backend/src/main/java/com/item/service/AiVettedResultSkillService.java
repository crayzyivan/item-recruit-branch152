package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.AiVettedResultSkillEntity;

import java.util.List;

/**
 * AI审核技能明细表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:18
 */
public interface AiVettedResultSkillService extends IService<AiVettedResultSkillEntity> {

    /**
     * 根据 ai审核结果表id 获取技能明细
     *
     * @param vettedResultIds ai审核结果表id
     * @return 技能明细
     */
    List<AiVettedResultSkillEntity> listByVettedResultIds(List<Long> vettedResultIds);

    /**
     * 删除
     * @param vettedResultId
     */
    void deleteByVettedResultId(Long vettedResultId);

}