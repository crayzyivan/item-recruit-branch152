package com.item.convert;

import com.item.dto.report.TopReportDTO;
import com.item.vo.report.TopViewDataVO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/23
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TopReportConverter {
    TopReportConverter INSTANCE = Mappers.getMapper(TopReportConverter.class);

    @Mapping(source = "applicationTotalCount", target = "candidateTotalCount")
    @Mapping(source = "pendingReviewCount", target = "pendingReviewTotalCount")
    @Mapping(source = "deniedTotalCount", target = "deniedTotalCount")
    @Mapping(source = "readyTotalCount", target = "readyTotalCount")
    TopViewDataVO convertToVO(TopReportDTO dto);
}
