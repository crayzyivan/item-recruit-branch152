package com.item.convert;

import com.item.dto.background.UserBackgroundCheckDTO;
import com.item.entity.BackgroundDataEntity;

import com.item.vo.BackgroundListVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BackgroundConverter {
    BackgroundConverter INSTANCE = Mappers.getMapper(BackgroundConverter.class);
    UserBackgroundCheckDTO converToDTO(BackgroundDataEntity entity);
    BackgroundListVO converToListVO(BackgroundDataEntity entityList);
}
