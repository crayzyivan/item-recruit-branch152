package com.item.convert;

import com.item.dto.EmploymentHistoryDTO;
import com.item.entity.EmploymentHistoryEntity;
import com.item.vo.EmploymentDetailsVO;
import com.item.vo.EmploymentHistoryVO;
import com.item.vo.InviteInterviewEmploymentHistoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmploymentHistoryConverter {
    EmploymentHistoryConverter INSTANCE = Mappers.getMapper(EmploymentHistoryConverter.class);

    EmploymentHistoryDTO convertEntityToDto(EmploymentHistoryEntity job);

    EmploymentHistoryDTO convertVoToDto(EmploymentHistoryVO job);

    EmploymentHistoryVO convertDtoToVo(EmploymentHistoryDTO job);

    EmploymentHistoryEntity convertDtoToEntity(EmploymentHistoryDTO job);

    List<EmploymentHistoryVO> dtoListToVOList(List<EmploymentHistoryDTO> dtos);

    List<EmploymentHistoryEntity> dtoListToEntityList(List<EmploymentHistoryDTO> dtos);

    List<EmploymentDetailsVO> entityListToDetailsVOList(List<EmploymentHistoryEntity> entities);

    /**
     * 将InviteInterviewEmploymentHistoryVO转换为EmploymentHistoryDTO
     * 用于邀请面试功能中的工作经历转换
     */
    EmploymentHistoryDTO convertInviteInterviewEmploymentHistoryVOToDto(InviteInterviewEmploymentHistoryVO vo);
}
