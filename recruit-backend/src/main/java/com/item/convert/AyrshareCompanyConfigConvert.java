package com.item.convert;

import com.item.dto.ayrshare.AyrshareTokenDTO;
import com.item.entity.AyrshareCompanyConfigEntity;
import com.item.util.CommonUtils;
import com.item.vo.ayrshare.AyrshareTokenVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Ayrshare配置对象转换器
 *
 * @author lh
 * @since 2025-08-28
 */
@Mapper(componentModel = "spring")
public interface AyrshareCompanyConfigConvert {

    /**
     * Entity转换为DTO
     */
    AyrshareTokenDTO entityToDTO(AyrshareCompanyConfigEntity entity);

    /**
     * DTO转换为Entity
     */
    AyrshareCompanyConfigEntity dtoToEntity(AyrshareTokenDTO dto);

    /**
     * DTO转换为VO，应用掩码显示逻辑
     */
    @Mapping(target = "ayrshareApiKey", qualifiedByName = "maskApiKey")
    AyrshareTokenVO dtoToVO(AyrshareTokenDTO dto);

    /**
     * 掩码显示API Key
     */
    @Named("maskApiKey")
    static String maskApiKey(String apiKey) {
        return CommonUtils.maskAyrshareToken(apiKey);
    }
}
