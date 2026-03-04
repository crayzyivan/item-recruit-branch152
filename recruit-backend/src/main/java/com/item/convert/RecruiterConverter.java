package com.item.convert;

import com.item.dto.RecruiterDTO;
import com.item.entity.RecruiterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RecruiterConverter {
    RecruiterConverter INSTANCE = Mappers.getMapper(RecruiterConverter.class);

    RecruiterDTO convertEntityToDto(RecruiterEntity job);

}
