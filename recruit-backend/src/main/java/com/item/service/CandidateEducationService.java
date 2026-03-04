package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.CandidateEducationEntity;

public interface CandidateEducationService extends IService<CandidateEducationEntity> {

    boolean deleteByCandidateId(Long candidateId);

}