package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.CandidateEducationEntity;
import com.item.entity.EmploymentHistoryEntity;
import com.item.mapper.CandidateEducationMapper;
import com.item.service.CandidateEducationService;
import org.springframework.stereotype.Service;

@Service
public class CandidateEducationServiceImpl extends ServiceImpl<CandidateEducationMapper, CandidateEducationEntity> implements CandidateEducationService {
    @Override
    public boolean deleteByCandidateId(Long candidateId) {
        return this.remove(new LambdaQueryWrapper<CandidateEducationEntity>().eq(CandidateEducationEntity::getCandidateId, candidateId));
    }
}
