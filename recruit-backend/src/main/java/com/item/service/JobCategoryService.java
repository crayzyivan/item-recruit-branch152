package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.JobCategoryDto;
import com.item.entity.JobCategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
public interface JobCategoryService extends IService<JobCategoryEntity> {

    /**
     * 返回所有的category 根据语言环境
     *
     * @return
     */
    List<JobCategoryDto> getAllJobCategories();

    /**
     * 返回所有的category id:name 根据语言环境
     *
     * @return
     */
    Map<Integer, String> getAllJobCategoryMapping();
}
