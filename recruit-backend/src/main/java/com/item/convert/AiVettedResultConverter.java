package com.item.convert;

import com.item.dto.AiVettedResultDTO;
import com.item.vo.AiVettedResultVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * AI审核结果 转换
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  18:28
 */
@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AiVettedResultConverter {
    AiVettedResultConverter INSTANCE = Mappers.getMapper(AiVettedResultConverter.class);

    List<AiVettedResultVO> toDtoList(List<AiVettedResultDTO> dtoList);

}