package com.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.entity.RecommendationCandidateJobEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Recommendation candidate job mapper
 * Provides basic CRUD operations for recommendation records
 *
 * @author hua.liu
 * @since 2025-10-23
 */
@Mapper
public interface RecommendationCandidateJobMapper extends BaseMapper<RecommendationCandidateJobEntity> {
    // Basic CRUD operations are provided by BaseMapper
    // For complex queries, add custom methods here using @Select annotation or XML mapping
}

