package com.item.convert;

import com.item.dto.ai.InterviewResponseDTO;
import com.item.vo.ai.InterviewResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GenerateInterviewLinkConvert {
    GenerateInterviewLinkConvert INSTANCE = Mappers.getMapper(GenerateInterviewLinkConvert.class);
    InterviewResponseDTO toAiCallbackDTO(InterviewResponseVO interviewResponseVO);

}
