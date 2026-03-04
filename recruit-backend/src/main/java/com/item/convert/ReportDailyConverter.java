package com.item.convert;

import java.util.List;

import com.item.dto.report.ReportDailyDTO;
import com.item.entity.ReportDailyEntity;
import com.item.vo.report.ReportDailyVO;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReportDailyConverter {
    ReportDailyConverter INSTANCE = Mappers.getMapper(ReportDailyConverter.class);

    ReportDailyDTO convertEntityToDTO(ReportDailyEntity entity);
    List<ReportDailyDTO> convertEntityListToDTOList(List<ReportDailyEntity> entity);
    List<ReportDailyVO> convertDTOListToVOList(List<ReportDailyDTO> dotList);
}
