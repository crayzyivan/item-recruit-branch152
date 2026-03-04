package com.item.service;

import com.item.entity.ApplicationScreeningReportsEntity;
import com.item.vo.ApplicationsSyncVO;

import java.util.List;
import java.util.UUID;

/**
 * AI筛选报告服务接口
 * 
 * 提供AI筛选报告相关的业务操作，使用MyBatis Plus的QueryWrapper实现复杂查询。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
public interface ApplicationScreeningReportsService {

    /**
     * 根据申请ID查询AI筛选报告
     * 
     * @param applicationId 申请ID
     * @return AI筛选报告列表
     */
    ApplicationScreeningReportsEntity getReportByApplicationId(UUID applicationId);

    /**
     * 查询全部的关联id
     * @return
     */
    List<String> listAllApplicationIds();


    List<ApplicationScreeningReportsEntity> listBySyncVo(ApplicationsSyncVO syncVo);

}
