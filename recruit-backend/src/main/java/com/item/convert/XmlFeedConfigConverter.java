package com.item.convert;

import com.item.dto.xmlfeed.XmlFeedConfigDTO;
import com.item.entity.XmlFeedConfigEntity;
import com.item.vo.feed.XmlFeedConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * XML Feed Configuration Converter
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface XmlFeedConfigConverter {
    XmlFeedConfigConverter INSTANCE = Mappers.getMapper(XmlFeedConfigConverter.class);

    /**
     * Convert Entity to VO
     *
     * @param entity XmlFeedConfigEntity
     * @return XmlFeedConfigVO
     */
    XmlFeedConfigVO convertEntityToVO(XmlFeedConfigEntity entity);

    /**
     * Convert Entity to DTO
     *
     * @param entity XmlFeedConfigEntity
     * @return XmlFeedConfigDTO
     */
    XmlFeedConfigDTO convertEntityToDTO(XmlFeedConfigEntity entity);

    /**
     * Convert DTO to Entity
     *
     * @param dto XmlFeedConfigDTO
     * @return XmlFeedConfigEntity
     */
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    XmlFeedConfigEntity convertDTOToEntity(XmlFeedConfigDTO dto);
}
