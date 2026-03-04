package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.AiVettedResultSkillEntity;
import com.item.entity.ApplicationsEntity;
import com.item.mapper.AiVettedResultSkillMapper;
import com.item.pgmapper.PgApplicationsMapper;
import com.item.service.ApplicationsService;
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
 * 关联表服务接口
 *
 * 提供关联表相关的业务操作，使用MyBatis Plus的QueryWrapper实现复杂查询。
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-11
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ApplicationsServiceImpl extends ServiceImpl<PgApplicationsMapper, ApplicationsEntity>  implements ApplicationsService {

    private final PgApplicationsMapper pgApplicationsMapper;

    /**
     * 根据更新时间字段的开始时间查询
     * @param updateStartDate
     * @return
     */
    @Override
    public List<ApplicationsEntity> listByUpdateStartDate(LocalDateTime updateStartDate) {
        LambdaQueryWrapper<ApplicationsEntity>  queryWrapper = new LambdaQueryWrapper<>();
        if (updateStartDate!=null){
            queryWrapper.ge(ApplicationsEntity::getUpdatedOn, updateStartDate);
        }
        queryWrapper.orderByAsc(ApplicationsEntity::getUpdatedOn);
        return pgApplicationsMapper.selectList(queryWrapper);
    }

    /**
     * 根据查询条件查询
     * @param syncVo
     * @return
     */
    @Override
    public List<ApplicationsEntity> listBySyncVo(ApplicationsSyncVO syncVo) {
        LambdaQueryWrapper<ApplicationsEntity>  queryWrapper = new LambdaQueryWrapper<>();
        if (syncVo.getCreateStartTime()!=null){
            queryWrapper.ge(ApplicationsEntity::getCreatedOn, syncVo.getCreateStartTime());
        }
        if (syncVo.getCreateEndTime()!=null){
            queryWrapper.le(ApplicationsEntity::getCreatedOn, syncVo.getCreateEndTime());
        }
        if (syncVo.getUpdateStartTime()!=null){
            queryWrapper.ge(ApplicationsEntity::getUpdatedOn, syncVo.getUpdateStartTime());
        }
        if (syncVo.getUpdateEndTime()!=null){
            queryWrapper.le(ApplicationsEntity::getUpdatedOn, syncVo.getUpdateEndTime());
        }
        if (CollectionUtils.isNotEmpty(syncVo.getApplicationIds())){
            List<UUID> applicationIds=syncVo.getApplicationIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            queryWrapper.in(ApplicationsEntity::getId, applicationIds);
        }
        queryWrapper.orderByAsc(ApplicationsEntity::getUpdatedOn);
        return pgApplicationsMapper.selectList(queryWrapper);

    }
}