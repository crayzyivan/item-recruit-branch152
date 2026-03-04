package com.item.service.impl;

import com.item.dto.CountryDTO;
import com.item.dto.ai.InterviewDataDTO;
import com.item.dto.ai.InterviewRequestDTO;
import com.item.dto.ai.QuestionGenerationDTO;
import com.item.dto.ayrshare.AyrSharePostDTO;
import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.dto.job.JobCreateBO;
import com.item.framework.config.AiInterviewConfig;
import com.item.framework.constant.HotListType;
import com.item.framework.constant.InterviewTypeEnum;
import com.item.service.AyrShareService;
import com.item.service.GenerateUrlCodeService;
import com.item.service.JobPostProcessingService;
import com.item.service.LocationService;
import com.item.service.ShortIdGenerator;
import com.item.util.CommonUtils;

import com.item.vo.ai.InterviewResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of JobPostProcessingService
 * Handles job post-processing operations like AyrShare posting and AI interview creation
 *
 * @author system
 * @since 1.0.0
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class JobPostProcessingServiceImpl implements JobPostProcessingService {
    
    @Value("${company.name.replace.str:,.，。#@%$}")
    private String companyNameReplace;
    
    @Value("${job.publish.url:https://recruit-dev.item.pub}")
    private String jobPublishUrl;
    
    @Value("${job.publish.route.info.prefix:job-details}")
    private String jobPublishRouteInfoPrefix;
    
    @Value("${job.publish.enable.ayr.share:false}")
    private boolean jobPublishAyrShare;
    
    @Value("${job.publish.share.case:{0} is hiring for {1}. Click {2} to apply for the job.}")
    private String jobPublishShareCase;
    
    @Value("${job.publish.share.title.case:{0} is hiring for {1}.}")
    private String jobPublishShareTitleCase;
    
    private final AyrShareService ayrShareService;
    private final ShortIdGenerator shortIdGenerator;
    private final GenerateUrlCodeService generateUrlCodeService;
    private final AIServiceImpl aiService;
    private final LocationService locationService;
    private final AiInterviewConfig aiInterviewConfig;
    
    @Override
    public void ayrSharePost(JobCreateBO jobCreateBO, IamCompanyDetailDTO companyInfo, Long jobId) {
        log.info("ayrShare jobPublishAyrShare {} jobPublishUrl {} jobPublishUrlPrefix {} companyNameReplace {}", 
            jobPublishAyrShare, jobPublishUrl, jobPublishRouteInfoPrefix, companyNameReplace);
            
        if (jobPublishAyrShare && HotListType.isEnabled(jobCreateBO.getHotList())) {
            String idStr = shortIdGenerator.generateShortId(jobId);
            String generateUrlCode = generateUrlCodeService.generateUrlCodeByConfig(
                jobCreateBO.getTitle(), jobCreateBO.getUrlCode(), companyInfo.getCompanyName(), companyNameReplace);
            String publishURLIdStr = generateInfoShareLink(idStr, generateUrlCode);
            String message = MessageFormat.format(jobPublishShareCase, companyInfo.getCompanyName(), jobCreateBO.getTitle(), publishURLIdStr);
            
            AyrSharePostDTO ayrSharePostDTO = new AyrSharePostDTO();
            ayrSharePostDTO.setJobId(jobId);
            ayrSharePostDTO.setCompanyName(companyInfo.getCompanyName());
            ayrSharePostDTO.setCompanyCode(companyInfo.getCompanyCode());
            ayrSharePostDTO.setJobTitle(jobCreateBO.getTitle());
            ayrSharePostDTO.setJobPublishShareCase(jobPublishShareCase);
            ayrSharePostDTO.setJobPublishShareTitleCase(jobPublishShareTitleCase);
            ayrSharePostDTO.setPublishUrl(publishURLIdStr);
            
            log.info("ayrSharePost ayrSharePostDTO {}", ayrSharePostDTO);
            // Use the new CompletableFuture method, get config info from database and update Job status
            ayrShareService.asyncSendToAyrShareWithConfig(ayrSharePostDTO);
        }
    }
    
    @Override
    public InterviewResultVO createAIInterview(JobCreateBO jobCreateBO) {
        //创建ai面试
        InterviewRequestDTO interviewRequestDTO = new InterviewRequestDTO();
        InterviewDataDTO interviewDataDTO = new InterviewDataDTO();
        QuestionGenerationDTO questionGeneration =  new QuestionGenerationDTO();
        interviewRequestDTO.setOrganizationName(aiInterviewConfig.getOrganizationName());
        interviewRequestDTO.setInterviewData(interviewDataDTO);
        interviewRequestDTO.setQuestionGeneration(questionGeneration);
        interviewRequestDTO.setCustomQuestions(jobCreateBO.getCustomQuestions());
        interviewDataDTO.setInterviewType(InterviewTypeEnum.getByCode(jobCreateBO.getInterviewType()).getName());
        interviewDataDTO.setInterviewerId(aiInterviewConfig.getInterviewerId());
        interviewDataDTO.setLogoUrl(aiInterviewConfig.getLogoUrl());
        interviewDataDTO.setIsAnonymous(aiInterviewConfig.getIsAnonymous());
        interviewDataDTO.setUserId(aiInterviewConfig.getUserId());
        interviewDataDTO.setOrganizationId(aiInterviewConfig.getOrganizationId());
        interviewDataDTO.setResponseCount(aiInterviewConfig.getResponseCount());
        interviewDataDTO.setTimeDuration(jobCreateBO.getInterviewLength());
        interviewDataDTO.setName(jobCreateBO.getTitle());
        interviewDataDTO.setDescription(jobCreateBO.getJobDetail());
        interviewDataDTO.setCanChangeInterviewLanguage(true);
        interviewDataDTO.setEnableWrittenTest(jobCreateBO.getEnableWrittenTest());

        interviewRequestDTO.setEnablePersonalityTest(jobCreateBO.getPersonalityTestEnabled());
        interviewRequestDTO.setCustomQuestionsOnly(jobCreateBO.getCustomQuestionEnabled());
        interviewRequestDTO.setEnableMidwayScoring(jobCreateBO.getCheckpointEnabled());

        //String countryName = jobCreateBO.getLocations().isEmpty()?null:jobCreateBO.getLocations().getFirst().getCountryName();
        String countryName=null;
        if (CollectionUtils.isNotEmpty(jobCreateBO.getLocations())){
            List<CountryDTO> countryDTOS = locationService.listEnCountryByCountryIds(Arrays.asList(jobCreateBO.getLocations().getFirst().getCountryId()));
            if (CollectionUtils.isNotEmpty(countryDTOS)){
                countryName=countryDTOS.getFirst().getName();
            }
        }
        interviewDataDTO.setObjective(countryName);
        questionGeneration.setDimensions(jobCreateBO.getSkills());
        questionGeneration.setNumber(aiInterviewConfig.getQuestionNumber());
        return aiService.createInterview(interviewRequestDTO,jobCreateBO);
    }
    
    private String generateInfoShareLink(String idCode, String urlCode) {
        // Build publish URL following arySharePost method pattern
        return CommonUtils.joinInclinedRod(jobPublishUrl, jobPublishRouteInfoPrefix, idCode, urlCode);
    }
}
