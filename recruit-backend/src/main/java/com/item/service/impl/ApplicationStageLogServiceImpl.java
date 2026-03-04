package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.entity.ApplicationStageLogEntity;
import com.item.pgmapper.PgApplicationStageLogMapper;
import com.item.service.ApplicationStageLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * ApplicationStageLog服务实现类
 * 
 * 处理PostgreSQL candidates.application_stage_log表的数据操作，
 * 提供申请阶段日志的基本CRUD功能。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ApplicationStageLogServiceImpl implements ApplicationStageLogService {

    private final PgApplicationStageLogMapper  pgApplicationStageLogMapper;

    @Override
    public List<ApplicationStageLogEntity> listByApplicationId(UUID applicationId) {
        LambdaQueryWrapper<ApplicationStageLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApplicationStageLogEntity::getApplicationId, applicationId);
        queryWrapper.orderByDesc(ApplicationStageLogEntity::getId);
        return pgApplicationStageLogMapper.selectList(queryWrapper);
    }
}
