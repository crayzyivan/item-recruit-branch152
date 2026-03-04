package com.item.convert;

import com.item.dto.ayrshare.AyrShareJwtResponseDTO;
import com.item.vo.ayrshare.AyrShareJwtResponseVO;
import org.mapstruct.Mapper;

/**
 * @author : lh
 */
@Mapper(componentModel = "spring")
public interface AyrShareConverter {
    AyrShareJwtResponseVO toJwtVO(AyrShareJwtResponseDTO ayrShareJwtResponseDTO);
}
