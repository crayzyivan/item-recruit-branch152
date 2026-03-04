package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.JobModeDto;
import com.item.entity.JobModeEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
public interface JobModeService extends IService<JobModeEntity> {

    /**
     * 返回所有的mode 根据语言环境
     *
     * @return
     */
    List<JobModeDto> getAll();

    /**
     * 返回所有的mode id：name 根据语言环境
     * @return
     */
    Map<Integer, String> getAllJobModeMapping();
}
