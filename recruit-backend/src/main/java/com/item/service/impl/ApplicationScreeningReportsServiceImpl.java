package com.item.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.entity.ApplicationScreeningReportsEntity;
import com.item.pgmapper.PgApplicationScreeningReportsMapper;
import com.item.service.ApplicationScreeningReportsService;
import com.item.vo.ApplicationsSyncVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI筛选报告服务实现类
 * 
 * 使用MyBatis Plus的QueryWrapper实现复杂查询，替代直接的SQL语句。
 * 注入PostgreSQL专用的mapper，确保使用正确的数据源。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ApplicationScreeningReportsServiceImpl implements ApplicationScreeningReportsService {

    private final PgApplicationScreeningReportsMapper screeningReportsMapper;

    @Override
    public ApplicationScreeningReportsEntity getReportByApplicationId(UUID applicationId) {
        LambdaQueryWrapper<ApplicationScreeningReportsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplicationScreeningReportsEntity::getApplicationId, applicationId)
               .orderByDesc(ApplicationScreeningReportsEntity::getScreeningDate)
                .last("limit 1");
        return screeningReportsMapper.selectOne(wrapper);
    }

    /**
     * 查询全部的关联id
     * @return
     */
    @Override
    public List<String> listAllApplicationIds() {
        LambdaQueryWrapper<ApplicationScreeningReportsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ApplicationScreeningReportsEntity::getApplicationId);
        List<ApplicationScreeningReportsEntity> entities = screeningReportsMapper.selectList(wrapper);
        return entities.stream()
                .map(ApplicationScreeningReportsEntity::getApplicationId)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationScreeningReportsEntity> listBySyncVo(ApplicationsSyncVO syncVo) {
        LambdaQueryWrapper<ApplicationScreeningReportsEntity> wrapper = new LambdaQueryWrapper<>();
        if (CollectionUtils.isNotEmpty(syncVo.getApplicationIds())){
            List<UUID> uuidList = syncVo.getApplicationIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            wrapper.in(ApplicationScreeningReportsEntity::getApplicationId, uuidList);
        }
        if (syncVo.getCreateStartTime() != null){
            wrapper.ge(ApplicationScreeningReportsEntity::getScreeningDate, syncVo.getCreateStartTime());
        }
        if (syncVo.getCreateEndTime() != null){
            wrapper.le(ApplicationScreeningReportsEntity::getScreeningDate, syncVo.getCreateEndTime());
        }
        return screeningReportsMapper.selectList(wrapper);
    }
}
