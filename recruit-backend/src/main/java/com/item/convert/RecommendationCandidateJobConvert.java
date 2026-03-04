package com.item.convert;

import com.item.dto.SaveRecommendationDTO;
import com.item.entity.RecommendationCandidateJobEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Recommendation candidate job convert
 * Provides conversion between DTO and Entity
 *
 * @author hua.liu
 * @since 2025-10-23
 */
@Mapper(componentModel = "spring")
public interface RecommendationCandidateJobConvert {

    RecommendationCandidateJobConvert INSTANCE = Mappers.getMapper(RecommendationCandidateJobConvert.class);

    /**
     * Convert save DTO to entity
     *
     * @param saveDTO save recommendation DTO
     * @return entity
     */
    RecommendationCandidateJobEntity saveDTOToEntity(SaveRecommendationDTO saveDTO);

}

