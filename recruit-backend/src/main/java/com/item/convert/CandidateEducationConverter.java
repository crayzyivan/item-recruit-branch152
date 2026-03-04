package com.item.convert;

import com.item.dto.CandidateEducationDTO;
import com.item.entity.CandidateEducationEntity;
import com.item.vo.CandidateEducationVO;
import com.item.vo.EducationDetailsVO;
import com.item.vo.InviteInterviewEducationVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CandidateEducationConverter {
    CandidateEducationConverter INSTANCE = Mappers.getMapper(CandidateEducationConverter.class);

    CandidateEducationDTO convertEntityToDto(CandidateEducationEntity job);

    CandidateEducationDTO convertVoToDto(CandidateEducationVO job);

    CandidateEducationVO convertDtoToVo(CandidateEducationDTO job);

    CandidateEducationEntity convertDtoToEntity(CandidateEducationDTO job);

    List<CandidateEducationVO> dtoListToVOList(List<CandidateEducationDTO> dtos);

    List<CandidateEducationEntity> dtoListToEntityList(List<CandidateEducationDTO> dtos);

    EducationDetailsVO convertEntityToDetailsVO(CandidateEducationEntity candidateEducationEntity);

    /**
     * 将InviteInterviewEducationVO转换为CandidateEducationDTO
     * 用于邀请面试功能中的教育经历转换
     */
    CandidateEducationDTO convertInviteInterviewEducationVOToDto(InviteInterviewEducationVO vo);

}
