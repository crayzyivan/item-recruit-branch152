package com.item.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.JobScreenEntity;
import com.item.mapper.JobScreenMapper;
import com.item.service.JobScreenService;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
@Service
public class JobScreenServiceImp extends ServiceImpl<JobScreenMapper, JobScreenEntity> implements JobScreenService {

    @Override
    public long getApplicationCount(long jobId) {
        return this.count(new QueryWrapper<JobScreenEntity>().lambda().eq(JobScreenEntity::getJobId,jobId));
    }
}
