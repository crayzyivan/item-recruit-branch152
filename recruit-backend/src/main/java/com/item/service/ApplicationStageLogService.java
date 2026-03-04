package com.item.service;

import com.item.entity.ApplicationStageLogEntity;

import java.util.List;
import java.util.UUID;

/**
 * ApplicationStageLog服务接口
 * 
 * 处理PostgreSQL candidates.application_stage_log表的数据操作，
 * 提供申请阶段日志的基本CRUD功能。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
public interface ApplicationStageLogService {

    List<ApplicationStageLogEntity> listByApplicationId(UUID applicationId);

}
