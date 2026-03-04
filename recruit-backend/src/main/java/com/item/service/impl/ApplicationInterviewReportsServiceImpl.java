package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.item.entity.ApplicationInterviewReportsEntity;
import com.item.pgmapper.PgApplicationInterviewReportsMapper;
import com.item.service.ApplicationInterviewReportsService;
import com.item.vo.ApplicationsSyncVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 面试报告服务实现类
 * 
 * 使用MyBatis Plus的QueryWrapper实现复杂查询，替代直接的SQL语句。
 * 注入PostgreSQL专用的mapper，确保使用正确的数据源。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-28
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ApplicationInterviewReportsServiceImpl implements ApplicationInterviewReportsService {

    private final PgApplicationInterviewReportsMapper mapper;


    @Override
    public List<ApplicationInterviewReportsEntity> listByApplicationId(UUID applicationId) {
        LambdaQueryWrapper<ApplicationInterviewReportsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplicationInterviewReportsEntity::getApplicationId, applicationId)
               .orderByDesc(ApplicationInterviewReportsEntity::getCreatedOn);
        return mapper.selectList(wrapper);
    }

    @Override
    public ApplicationInterviewReportsEntity getByApplicationId(UUID applicationId) {
        LambdaQueryWrapper<ApplicationInterviewReportsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplicationInterviewReportsEntity::getApplicationId, applicationId)
                .orderByDesc(ApplicationInterviewReportsEntity::getCreatedOn)
                .last("LIMIT 1");;
        return mapper.selectOne(wrapper);
    }

    @Override
    public List<ApplicationInterviewReportsEntity> listAll() {
        LambdaQueryWrapper<ApplicationInterviewReportsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ApplicationInterviewReportsEntity::getCreatedOn);
        return mapper.selectList(wrapper);
    }

    @Override
    public List<ApplicationInterviewReportsEntity> listByStartDate(LocalDateTime startDate) {
        LambdaQueryWrapper<ApplicationInterviewReportsEntity> wrapper = new LambdaQueryWrapper<>();
        if (startDate!=null) {
            wrapper.ge(ApplicationInterviewReportsEntity::getCreatedOn,startDate);
        }
        wrapper.orderByDesc(ApplicationInterviewReportsEntity::getCreatedOn);
        return mapper.selectList(wrapper);
    }

    @Override
    public List<ApplicationInterviewReportsEntity> listBySyncVo(ApplicationsSyncVO syncVo) {
        LambdaQueryWrapper<ApplicationInterviewReportsEntity> wrapper = new LambdaQueryWrapper<>();
        if (syncVo.getCreateStartTime()!=null) {
            wrapper.ge(ApplicationInterviewReportsEntity::getCreatedOn,syncVo.getCreateStartTime());
        }
        if (syncVo.getCreateEndTime()!=null) {
            wrapper.le(ApplicationInterviewReportsEntity::getCreatedOn,syncVo.getCreateEndTime());
        }
        if (CollectionUtils.isNotEmpty(syncVo.getApplicationIds())){
            List<UUID> uuidList = syncVo.getApplicationIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            wrapper.in(ApplicationInterviewReportsEntity::getApplicationId, uuidList);
        }
        wrapper.orderByDesc(ApplicationInterviewReportsEntity::getCreatedOn);
        return mapper.selectList(wrapper);
    }
}
