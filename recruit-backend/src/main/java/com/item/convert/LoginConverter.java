package com.item.convert;

import com.item.dto.LoginDTO;
import com.item.vo.LoginRequestVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LoginConverter {
    LoginConverter INSTANCE = Mappers.getMapper(LoginConverter.class);

    LoginDTO convertVoToDto(LoginRequestVO job);

}
