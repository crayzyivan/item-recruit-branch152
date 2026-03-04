package com.item.convert;

import com.item.dto.CandidateJobCountDTO;
import com.item.entity.CandidateJobCountEntity;
import com.item.vo.CandidateJobCountVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CandidateJobCountConvert {
    CandidateJobCountConvert INSTANCE = Mappers.getMapper(CandidateJobCountConvert.class);

    CandidateJobCountDTO toDto(CandidateJobCountEntity entity);

    CandidateJobCountVO toVo(CandidateJobCountDTO entity);

}