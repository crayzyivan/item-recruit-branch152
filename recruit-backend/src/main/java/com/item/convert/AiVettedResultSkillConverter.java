package com.item.convert;

import com.item.dto.AiVettedResultSkillDTO;
import com.item.entity.AiVettedResultSkillEntity;
import com.item.vo.AiVettedResultSkillVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * AI审核技能明细转换
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  18:28
 */
@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AiVettedResultSkillConverter {
    AiVettedResultSkillConverter INSTANCE = Mappers.getMapper(AiVettedResultSkillConverter.class);

    AiVettedResultSkillDTO toDto(AiVettedResultSkillEntity entity);

    List<AiVettedResultSkillDTO> toDtoList(List<AiVettedResultSkillEntity> entityList);

    List<AiVettedResultSkillVO> toSkillVoList(List<AiVettedResultSkillEntity> entityList);
    AiVettedResultSkillVO toSkillVo(AiVettedResultSkillEntity entity);
}