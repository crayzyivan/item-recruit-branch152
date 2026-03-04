package com.item.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.item.dto.JobCategoryDto;
import com.item.vo.JobCategoryVo;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface JobCategoryConverter {
    JobCategoryConverter INSTANCE = Mappers.getMapper(JobCategoryConverter.class);
    List<JobCategoryVo> convertToListVo(List<JobCategoryDto> dtoList);
}
