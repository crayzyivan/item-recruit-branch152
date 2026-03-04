package com.item.convert;

import com.item.dto.LeadsCustomerCompanyDTO;
import com.item.entity.LeadsCustomerCompanyEntity;
import com.item.vo.LeadsCustomerCompanyVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * <p>
 * lead customer company 映射关系转换器
 * </p>
 *
 * @author lh on 2025/7/29
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface LeadsCustomerCompanyConvert {
    
    LeadsCustomerCompanyConvert INSTANCE = Mappers.getMapper(LeadsCustomerCompanyConvert.class);
    
    /**
     * Entity to DTO
     */
    LeadsCustomerCompanyDTO entityToDto(LeadsCustomerCompanyEntity entity);
    
    /**
     * DTO to Entity
     */
    LeadsCustomerCompanyEntity dtoToEntity(LeadsCustomerCompanyDTO dto);
    
    /**
     * Entity to VO
     */
    LeadsCustomerCompanyVO entityToVo(LeadsCustomerCompanyEntity entity);
    
    /**
     * DTO to VO
     */
    LeadsCustomerCompanyVO dtoToVo(LeadsCustomerCompanyDTO dto);
    
    /**
     * Entity list to DTO list
     */
    List<LeadsCustomerCompanyDTO> entityListToDtoList(List<LeadsCustomerCompanyEntity> entityList);
    
    /**
     * DTO list to Entity list
     */
    List<LeadsCustomerCompanyEntity> dtoListToEntityList(List<LeadsCustomerCompanyDTO> dtoList);
    
    /**
     * Entity list to VO list
     */
    List<LeadsCustomerCompanyVO> entityListToVoList(List<LeadsCustomerCompanyEntity> entityList);
    
    /**
     * DTO list to VO list
     */
    List<LeadsCustomerCompanyVO> dtoListToVoList(List<LeadsCustomerCompanyDTO> dtoList);
}