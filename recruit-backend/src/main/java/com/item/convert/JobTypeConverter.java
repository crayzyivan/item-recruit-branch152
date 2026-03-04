package com.item.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.item.dto.JobTypeDto;
import com.item.vo.JobTypeVo;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface JobTypeConverter {
    JobTypeConverter INSTANCE = Mappers.getMapper(JobTypeConverter.class);

    JobTypeVo convertToVo(JobTypeDto dto);
    List<JobTypeVo> convertToListVo(List<JobTypeDto> dtos);
}
