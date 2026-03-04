package com.item.convert;

import java.util.List;

import com.item.dto.ai.ResumeAIMatchDTO;
import com.item.dto.ai.SendInterviewMailDTO;
import com.item.dto.job.CandidateJobApplyDto;
import com.item.dto.job.CandidateJobApplyRequestVO;
import com.item.dto.report.CandidateSimpleDTO;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.vo.CandidateJobVO;
import com.item.vo.ResumeAIscreenVO;
import com.item.vo.ai.JobMatchResultVO;
import com.item.vo.ai.SendInterviewMailVO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CandidateJobConvert {
    CandidateJobConvert INSTANCE = Mappers.getMapper(CandidateJobConvert.class);

    CandidateJobApplyDto toCandidateJobDTO(CandidateJobApplyRequestVO vo);
    @Mapping(target = "lastSendEmailTime" , source = "interviewStartTime")
    ResumeAIMatchDTO toEntitybDTO(CandidateJobEntity entity);

    List<ResumeAIscreenVO> dtoListToVOList(List<ResumeAIMatchDTO> dtos);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    void dtoToEntity(ResumeAIMatchDTO resumeAIMatchDTO, @MappingTarget JobMatchResultVO resumeMatchResultEntity);

    SendInterviewMailDTO toSendInterviewMailDTO(SendInterviewMailVO sendInterviewMailVO);

    List<CandidateSimpleDTO> toSimpleDTO(List<CandidateJobEntity> candidateJobEntity);

    List<ResumeAIMatchDTO> tolistDto(List<CandidateJobEntity> entities);

    @Mapping(source = "id", target = "candidateId")
    CandidateSimpleDTO candidateEntitieToSimpleDTO(CandidateEntity candidateEntitie);
    List<CandidateSimpleDTO> candidateEntitiesToSimpleDTOs(List<CandidateEntity> candidateEntities);
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(source = "createTime", target = "candidateCreateTime")
    @Mapping(source = "updateTime", target = "candidateUpdateTime")
    @Mapping(source = "countryId", target = "candidateCountryId")
    @Mapping(source = "stateId", target = "candidateStateId")
    @Mapping(source = "cityId", target = "candidateCityId")
    @Mapping(source = "countryName", target = "candidateCountryName")
    @Mapping(source = "stateName", target = "candidateStateName")
    @Mapping(source = "cityName", target = "candidateCityName")
    @Mapping(source = "id", target = "candidateId")
    @Mapping(target = "id", ignore = true)
    void candidateEntityToJobMatchResultVO(CandidateEntity candidateEntity, @MappingTarget JobMatchResultVO jobMatchResultVO);
    @Mapping(source = "createTime", target = "jobCreateTime")
    @Mapping(source = "updateTime", target = "jobUpdateTime")
    @Mapping(source = "deleted", target = "jobDeleted")
    @Mapping(source = "id", target = "jobId")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    void jobEntityToJobMatchResultVO(JobEntity jobEntity, @MappingTarget JobMatchResultVO jobMatchResultVO);

    @Mapping(source = "title", target = "jobTitle")
    @Mapping(source = "locationName", target = "jobLocation")
    @Mapping(source = "candidateCreateTime", target = "createTime")
    @Mapping(source = "candidateUpdateTime", target = "updateTime")
    CandidateSimpleDTO jobMatchResultVOToSimpleDTO(JobMatchResultVO jobMatchResultVO);

    @Mapping(source = "title", target = "jobTitle")
    @Mapping(source = "locationName", target = "jobLocation")
    @Mapping(source = "jobCreateTime", target = "createTime")
    @Mapping(source = "jobUpdateTime", target = "updateTime")
    CandidateSimpleDTO jobMatchResultVOToJobSimpleDTO(JobMatchResultVO jobMatchResultVO);

    @Mapping(source = "candidateCountryName", target = "countryName")
    @Mapping(source = "candidateStateName", target = "stateName")
    @Mapping(source = "candidateCityName", target = "cityName")
    @Mapping(source = "candidateCountryId", target = "countryId")
    @Mapping(source = "candidateStateId", target = "stateId")
    @Mapping(source = "candidateCityId", target = "cityId")
    CandidateJobVO jobMatchResultVOToCandidateJobVO(JobMatchResultVO jobMatchResultVO);
}