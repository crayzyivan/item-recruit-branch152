package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.EmploymentHistoryEntity;

public interface EmploymentHistoryService extends IService<EmploymentHistoryEntity> {

    boolean deleteByCandidateId(Long candidateId);

}