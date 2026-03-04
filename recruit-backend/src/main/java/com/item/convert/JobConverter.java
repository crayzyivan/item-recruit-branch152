package com.item.convert;

import com.item.dto.JobDto;
import com.item.entity.JobEntity;
import com.item.vo.JobListVO;
import com.item.vo.JobVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobConverter {
    JobConverter INSTANCE = Mappers.getMapper(JobConverter.class);

    @Mapping(target = "applicationCount", ignore = true)
    JobDto convertEntityToDto(JobEntity job);


    /**
     * DTO转Entity
     *
     * @param dto JobDTO
     * @return Job实体
     */
    @Mapping(target = "jobType", ignore = true)
    @Mapping(target = "jobCategory", ignore = true)
    JobEntity dtoToEntity(JobDto dto);

    /**
     * Entity列表转DTO列表
     *
     * @param entities Job实体列表
     * @return JobDTO列表
     */
    List<JobDto> entityListToDTOList(List<JobEntity> entities);

    /**
     * DTO列表转Entity列表
     *
     * @param dtos JobDTO列表
     * @return Job实体列表
     */
    List<JobEntity> dtoListToEntityList(List<JobDto> dtos);

    List<JobVO> dtoListToVOList(List<JobDto> dtos);

    @Mapping(source = "id", target = "jobId")
    JobVO jobDtoToJobVO(JobDto jobDto);

    @Mapping(source = "applicationCounts", target = "applicationCount")
    JobVO convert2JobVO(JobListVO jobListVO);

    List<JobVO> convert2JobVO(List<JobListVO> jobListVO);
}
