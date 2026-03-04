package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.CandidateEducationEntity;
import com.item.entity.EmploymentHistoryEntity;
import com.item.mapper.EmploymentHistoryMapper;
import com.item.service.EmploymentHistoryService;
import org.springframework.stereotype.Service;

@Service
public class EmploymentHistoryServiceImpl extends ServiceImpl<EmploymentHistoryMapper, EmploymentHistoryEntity> implements EmploymentHistoryService {
    @Override
    public boolean deleteByCandidateId(Long candidateId) {
        return this.remove(new LambdaQueryWrapper<EmploymentHistoryEntity>().eq(EmploymentHistoryEntity::getCandidateId, candidateId));
    }
}
