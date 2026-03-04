package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.JobTypeDto;
import com.item.entity.JobTypeEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * JobType服务
 * </p>
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
public interface JobTypeService extends IService<JobTypeEntity> {

    /**
     * 返回type 根据语言环境
     *
     * @return
     */
    List<JobTypeDto> getAllJobTypes();

    /**
     * 返回type的id：name 根据语言环境
     *
     * @return
     */
    Map<Integer, String> getAllJobTypeMapping();
}
