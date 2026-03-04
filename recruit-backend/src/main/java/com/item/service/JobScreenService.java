package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.JobScreenEntity;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
public interface JobScreenService extends IService<JobScreenEntity> {

    /**
     * 获取该职位对应候选人个数
     * @return
     */
    long getApplicationCount(long jobId);
}
