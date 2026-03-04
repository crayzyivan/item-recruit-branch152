package com.item.convert;

import com.item.dto.ai.AiCallbackDTO;
import com.item.entity.AiCallbackEntity;
import com.item.vo.ai.AiCallbackVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AiCallbackConvert {

    AiCallbackConvert INSTANCE = Mappers.getMapper(AiCallbackConvert.class);
    @Mapping(source = "applicationId", target = "candidateJobId")
    AiCallbackDTO toAiCallbackDTO(AiCallbackVO aiCallbackVO);

    AiCallbackVO toAiCallbackVO(AiCallbackDTO aiCallbackDTO);

    AiCallbackEntity toAiCallbackEntity(AiCallbackDTO aiCallbackDTO);

    AiCallbackDTO toAiCallbackDTO(AiCallbackEntity aiCallbackEntity);


}
