package com.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.JobStatusRecordEntity;
import org.apache.ibatis.annotations.Mapper;


/**
 * 招聘状态流转记录 mapper
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  17:21
 */
@Mapper
public interface JobStatusRecordMapper extends BaseMapper<JobStatusRecordEntity> {
} 