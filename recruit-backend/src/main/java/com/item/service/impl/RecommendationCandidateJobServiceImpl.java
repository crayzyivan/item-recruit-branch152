package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.item.convert.RecommendationCandidateJobConvert;
import com.item.dto.SaveRecommendationDTO;
import com.item.entity.RecommendationCandidateJobEntity;
import com.item.mapper.RecommendationCandidateJobMapper;
import com.item.service.RecommendationCandidateJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Recommendation candidate job service implementation
 * Implements business operations for recommendation records
 *
 * @author hua.liu
 * @since 2025-10-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationCandidateJobServiceImpl
        extends ServiceImpl<RecommendationCandidateJobMapper, RecommendationCandidateJobEntity>
        implements RecommendationCandidateJobService {

    private final RecommendationCandidateJobConvert recommendationConvert;

    @Override
    public RecommendationCandidateJobEntity saveRecommendation(SaveRecommendationDTO saveDTO) {
        // Convert DTO to Entity using MapStruct
        RecommendationCandidateJobEntity entity = recommendationConvert.saveDTOToEntity(saveDTO);
        
        this.save(entity);
        log.info("Saved recommendation record: jobId={}, candidateId={}, recommendBy={}", 
                saveDTO.getJobId(), saveDTO.getCandidateId(), saveDTO.getRecommendBy());
        
        return entity;
    }

    @Override
    public List<RecommendationCandidateJobEntity> listByJobIds(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return Lists.newArrayList();
        }
        LambdaQueryWrapper<RecommendationCandidateJobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RecommendationCandidateJobEntity::getJobId, jobIds);
        return this.list(wrapper);
    }

    @Override
    public List<RecommendationCandidateJobEntity> listByCandidateIds(List<Long> candidateIds) {
        if (CollectionUtils.isEmpty(candidateIds)) {
            return Lists.newArrayList();
        }
        LambdaQueryWrapper<RecommendationCandidateJobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RecommendationCandidateJobEntity::getCandidateId, candidateIds);
        return this.list(wrapper);
    }

    @Override
    public boolean checkRecommendationExists(Long jobId, Long candidateId) {
        LambdaQueryWrapper<RecommendationCandidateJobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecommendationCandidateJobEntity::getJobId, jobId)
                .eq(RecommendationCandidateJobEntity::getCandidateId, candidateId).last("LIMIT 1");
        return this.count(wrapper) > 0;
    }
}

