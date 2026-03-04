package com.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.JobAuditHistoryEntity;
import com.item.mapper.JobAuditHistoryMapper;
import com.item.service.JobAuditHistoryService;
import org.springframework.stereotype.Service;

@Service
public class JobAuditHistoryServiceImpl extends ServiceImpl<JobAuditHistoryMapper, JobAuditHistoryEntity> implements JobAuditHistoryService {
}

