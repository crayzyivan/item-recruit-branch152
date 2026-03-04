package com.item.service;

import com.item.entity.ApplicationInterviewReportsEntity;
import com.item.vo.ApplicationsSyncVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 面试报告服务接口
 * 
 * 提供面试报告相关的业务操作，使用MyBatis Plus的QueryWrapper实现复杂查询。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-28
 */
public interface ApplicationInterviewReportsService {

    /**
     * 根据申请ID查询面试报告
     * 
     * @param applicationId 申请ID
     * @return 面试报告列表
     */
    List<ApplicationInterviewReportsEntity> listByApplicationId(UUID applicationId);

    /**
     * 根据申请ID查询面试报告
     *
     * @param applicationId 申请ID
     * @return 面试报告
     */
    ApplicationInterviewReportsEntity getByApplicationId(UUID applicationId);


    List<ApplicationInterviewReportsEntity> listAll();

    List<ApplicationInterviewReportsEntity> listByStartDate(LocalDateTime startDate);

    List<ApplicationInterviewReportsEntity> listBySyncVo(ApplicationsSyncVO syncVo);

}
