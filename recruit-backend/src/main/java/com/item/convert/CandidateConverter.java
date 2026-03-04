package com.item.convert;

import com.item.dto.CandidateDTO;
import com.item.dto.iam.IamUserRegisterCandidateReqDTO;
import com.item.dto.iam.IamUserRegisterReqDTO;
import com.item.dto.iam.IamUserRegisterResDTO;
import com.item.entity.CandidateEducationEntity;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateEsEntity;
import com.item.vo.CandidateDetailsVO;
import com.item.vo.CandidateEducationVO;
import com.item.vo.CandidateFullDetailsVO;
import com.item.vo.CandidateProfileVO;
import com.item.vo.CandidateProfileEducationVO;
import com.item.vo.CandidateProfileEmploymentVO;
import com.item.vo.CandidateRegisterRequestVO;
import com.item.vo.CandidateRegisterResponseVO;
import com.item.vo.CandidateRequestVO;
import com.item.vo.CandidateVO;
import com.item.vo.InviteInterviewParsedResumeVO;
import com.item.vo.ai.CandidateAIVO;
import com.item.vo.ai.CandidateEducationAIVO;
import com.item.vo.ai.ResumeCandidateEducationVO;
import com.item.vo.ai.ResumeParsingRsultVO;
import com.item.vo.iam.IamUserRegisterCandidateResVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CandidateConverter {
    CandidateConverter INSTANCE = Mappers.getMapper(CandidateConverter.class);

    CandidateDTO convertEntityToDto(CandidateEntity job);

    @Mapping(source = "name", target = "candidateName")
    @Mapping(source = "email", target = "candidateEmail")
    CandidateDTO convertVoToDto(CandidateRegisterRequestVO job);

    @Mapping(source = "candidateName", target = "name")
    @Mapping(source = "candidateEmail", target = "email")
    CandidateRegisterResponseVO convertDtoToVo(CandidateDTO job);

    CandidateVO convertDtoToCandidateVO(CandidateDTO job);

    @Mapping(source = "email", target = "candidateEmail")
    CandidateDTO convertCandidateRequestVOToDto(CandidateRequestVO job);

    /**
     * 将InviteInterviewParsedResumeVO转换为CandidateDTO
     * 用于邀请面试功能中的简历解析结果转换
     * 注意：educationList和employmentList需要单独处理，因为类型不同
     */
    @Mapping(source = "email", target = "candidateEmail")
    @Mapping(source = "email", target = "candidatePermanentEmail")
    @Mapping(target = "educationList", ignore = true)
    @Mapping(target = "employmentList", ignore = true)
    CandidateDTO convertInviteInterviewParsedResumeVOToDto(InviteInterviewParsedResumeVO parsedResume);

    void dtoToEntity(CandidateDTO candidateDTO,@MappingTarget CandidateEntity candidateEntity);

    void entityToEsEntity(CandidateEntity candidateEntity, @MappingTarget CandidateEsEntity candidateEsEntity);

    CandidateDetailsVO convertEntityToDetailsVO(CandidateEntity candidateEntity);

    CandidateEntity dtoToEntity(CandidateDTO candidateDTO);

    IamUserRegisterReqDTO convert2Req(IamUserRegisterCandidateReqDTO registerCandidateReqDTO);
    IamUserRegisterCandidateResVO convert2Res(IamUserRegisterResDTO registerResDTO);

    @Mapping(ignore = true,target = "educationList")
    CandidateRequestVO convertToCandidateRequestVO(ResumeParsingRsultVO job);

    CandidateEducationVO convertToCandidateEducationVO(ResumeCandidateEducationVO job);

    CandidateFullDetailsVO convertToCandidateFullDetailsVO(CandidateEsEntity esEntity);

    /**
     * 将CandidateEsEntity转换为CandidateProfileVO
     * @param esEntity ES实体
     * @return 候选人个人资料VO
     */
    @Mapping(target = "email", source = "candidateEmail")
    @Mapping(target = "educationList", source = "candidateEducations")
    @Mapping(target = "employmentList", source = "employmentHistories")
    CandidateProfileVO convertToCandidateProfileVO(CandidateEsEntity esEntity);

    /**
     * 转换教育经历
     */
    @Mapping(target = "institutionType", source = "institutionTypeId")
    @Mapping(target = "degree", source = "degreeId")
    CandidateProfileEducationVO convertToCandidateProfileEducationVO(com.item.entity.CandidateEducationEntity entity);

    /**
     * 转换工作经历
     */
    @Mapping(target = "responsibilities", source = "keyResponsibilities")
    CandidateProfileEmploymentVO convertToCandidateProfileEmploymentVO(com.item.entity.EmploymentHistoryEntity entity);

    @Mapping(target = "expectedSalaryId", source = "expectedSalary")
    @Mapping(target = "expectedSalary", ignore = true)
    CandidateAIVO toCandidateAIVO(CandidateEsEntity esEntity);

    @Mapping(target = "graduatedId", source = "graduated")
    @Mapping(target = "graduated", ignore = true)
    CandidateEducationAIVO toCandidateEducationAIVO(CandidateEducationEntity entity);

}
