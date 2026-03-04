package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.convert.*;
import com.item.dto.*;
import com.item.dto.ai.*;
import com.item.dto.job.GeoPointDTO;
import com.item.dto.job.IntelligenceScoreRuleDTO;
import com.item.dto.CompanyInfoSimpleDTO;
import com.item.dto.job.JobCreateBO;
import com.item.entity.*;
import com.item.es.JobEsService;
import com.item.es.ResumeEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.AiInterviewConfig;
import com.item.framework.config.MinuteBasedConfig;
import com.item.framework.constant.*;
import com.item.framework.constant.AICallbackStatusEnum;
import com.item.framework.constant.AICallbackTypeEnum;
import com.item.framework.constant.CommonConstants;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.constant.ContentTypeEnum;
import com.item.framework.constant.DictionaryEnum;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.InterviewTypeEnum;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.framework.constant.JobIntelligenceSubStageEnum;
import com.item.framework.constant.PricingModelEnum;
import com.item.framework.constant.SoftSkillLevelEnum;
import com.item.framework.constant.TransactionNoTypeEnum;
import com.item.framework.constant.TransactionTypeEnum;
import com.item.framework.constant.*;
import com.item.framework.error.BusinessException;
import com.item.mapper.AICallbackMapper;
import com.item.mapper.CityMapper;
import com.item.mapper.CountryMapper;
import com.item.mapper.StateMapper;
import com.item.service.*;
import com.item.util.*;
import com.item.vo.CandidateEducationVO;
import com.item.vo.CandidateRequestVO;
import com.item.vo.PointLogVO;
import com.item.vo.ai.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    private final RestTemplate restTemplate;

    private final AiInterviewConfig aiInterviewConfig;

    private final ResumeEsService resumeEsService;

    private final JobEsService jobEsService;

    private final CandidateJobService  candidateJobService;

    private final AICallbackMapper aiCallbackMapper;

    private final JobService jobService;

    private final CandidateService candidateService;

    private final AiVettedResultService aiVettedResultService;

    private final AiVettedResultSkillService aiVettedResultSkillService;

    private final JobFlowService jobFlowService;

    private final TranscriptConverter transcriptConverter;

    private final PointService pointService;
    private final DictionaryService dictionaryService;

    private final CountryMapper countryMapper;
    private final StateMapper stateMapper;
    private final CityMapper cityMapper;

    private final MinuteBasedConfig minuteBasedConfig;

    private final RedisSerialNumberUtils redisSerialNumberUtils;

    private final PointsOperationLogService pointsOperationLogService;

    private final ThreadPoolTaskExecutor aiTaskExecutor;

    private final JobTypeService jobTypeService;

    private final JobModeService jobModeService;

    private final JobCategoryService jobCategoryService;

    private final ObjectMapper objectMapper;

    private final S3Utils s3Utils;

    private final LocationService locationService;

    private final CandidateJobDomainService candidateJobDomainService;

    private final TranscriptObjectConverter transcriptObjectConverter;

    private final AiCallbackService aiCallbackService;

    private final CompanyService companyService;

    private final CandidateRecallService candidateRecallService;

    private final IntelligentJudgmentService intelligentJudgmentService;

    @Override
    public JobMatchResultVO aiMatch(CandidateJobEntity candidateJobEntity) {
        CandidateAIVO candidateAIVO = CandidateConverter.INSTANCE.toCandidateAIVO(
                resumeEsService.searchResumeToEs(candidateJobEntity.getCandidateId()));
        if (Objects.isNull(candidateAIVO)) {
            log.error("We cannot find the candidate's information.candidateId:{}", candidateJobEntity.getCandidateId());
            throw new BusinessException(GlobalStatusCode.FAIL,"We cannot find the candidate's information.");
        }
        List<DictionaryEntity> dictionaryEntities = dictionaryService.list();
        Map<Long, DictionaryEntity> dictionaryMap = dictionaryEntities.stream()
                // 按type分组
                .collect(
                        // 每组内再按value分组，保留对应的实体
                        Collectors.toMap(
                                DictionaryEntity::getId,
                                entity -> entity,
                                // 若存在相同value的实体，保留第一个
                                (existing, replacement) -> existing
                        )
                );
        Optional.ofNullable(candidateAIVO.getCurrencyTypeId())
                .flatMap(currencyTypeId ->
                        Optional.ofNullable(dictionaryMap.get(currencyTypeId))
                )
                .map(DictionaryEntity::getValue)
                .ifPresent(candidateAIVO::setCurrencyType);
        Optional.ofNullable(candidateAIVO.getExpectedSalaryId())
                .flatMap(expectedSalaryId ->
                        Optional.ofNullable(dictionaryMap.get(expectedSalaryId))
                )
                .map(DictionaryEntity::getValue)
                .ifPresent(candidateAIVO::setExpectedSalary);
        Optional.ofNullable(candidateAIVO.getSalaryTypeId())
                .flatMap(salaryTypeId ->
                        Optional.ofNullable(dictionaryMap.get(salaryTypeId))
                )
                .map(DictionaryEntity::getValue)
                .ifPresent(candidateAIVO::setSalaryType);
        List<CandidateEducationAIVO> candidateEducations = candidateAIVO.getCandidateEducations();
        candidateEducations.forEach(education -> {
            Optional.ofNullable(education.getDegreeId())
                    .flatMap(degreeId ->
                            Optional.ofNullable(dictionaryMap.get(degreeId))
                    )
                    .map(DictionaryEntity::getValue)
                    .ifPresent(education::setDegree);
            Optional.ofNullable(education.getGraduatedId())
                    .flatMap(graduatedId ->
                            Optional.ofNullable(dictionaryMap.get(graduatedId))
                    )
                    .map(DictionaryEntity::getValue)
                    .ifPresent(education::setGraduated);
        });
        String resume;
        try {
            resume = objectMapper.writeValueAsString(candidateAIVO);
        } catch (JsonProcessingException e) {
            log.error("简历信息获取失败：{}",e.getMessage(),e);
            throw new RuntimeException(e);
        }
        JobEsEntity jobEsEntity = jobEsService.getJobById(candidateJobEntity.getJobId());
        if (Objects.isNull(jobEsEntity)) {
            log.error("We cannot find the jobEsEntity information.candidateId:{}", candidateJobEntity.getJobId());
            throw new BusinessException(GlobalStatusCode.FAIL,"We cannot find the jobEsEntity information.");
        }
//        String locationName =jobEsEntity.getLocationName();
//        if (candidateJobEntity.getJobCityId()!=null){
//            locationName= CommonUtils.getLocationName(candidateJobEntity.getJobCountryName(),candidateJobEntity.getJobStateName(),candidateJobEntity.getJobCityName());
//        }
        String countryName="";
        List<CountryDTO> countryDTOS = locationService.listEnCountryByCountryIds(Collections.singletonList(candidateJobEntity.getJobCountryId()));
        if (CollectionUtils.isNotEmpty(countryDTOS)){
            countryName=countryDTOS.getFirst().getName();
        }
        String stateName="";
        List<StateDTO> stateDTOS = locationService.listEnStateByStateIds(Collections.singletonList(candidateJobEntity.getJobStateId()));
        if (CollectionUtils.isNotEmpty(stateDTOS)){
            stateName=stateDTOS.getFirst().getName();
        }
        String cityName="";
        List<CityDTO> cityDTOS = locationService.listEnCityByCityIds(Collections.singletonList(candidateJobEntity.getJobCityId()));
        if (CollectionUtils.isNotEmpty(cityDTOS)){
            cityName=cityDTOS.getFirst().getName();
        }
        String locationName= CommonUtils.getLocationName(countryName,stateName,cityName);
        if(!StringUtils.hasText(locationName)){
            log.error("Location not selected when submitting resume:{}", candidateJobEntity.getJobId());
            throw new BusinessException(GlobalStatusCode.FAIL,"Location not selected when submitting resume.");
        }
        AiResumeScreenDTO  aiResumeScreenDTO = AiResumeScreenDTO.builder().jobTitle(jobEsEntity.getTitle())
                .skills(jobEsEntity.getSkills())
                .minimumRequirements(jobEsEntity.getMinimumJobRequirement())
                .responsibilities(jobEsEntity.getMainDuty())
                .location(locationName)
                .minimumSalary(String.valueOf(jobEsEntity.getMinSalary()))
                .maximumSalary(String.valueOf(jobEsEntity.getMaxSalary()))
                .language(countryName)
                .experience("无")
                .resume(resume).build();
        HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeadersByEsJob(jobEsEntity);
        HttpEntity<AiResumeScreenDTO> requestEntity =
                new HttpEntity<>(aiResumeScreenDTO, headers);
        ResponseEntity<JobMatchResultVO> result = restTemplate.postForEntity(aiInterviewConfig.getCalculateMatchRateUrl(), requestEntity, JobMatchResultVO.class);
        log.info("获取匹配度成功结果:关联id{},结果为:{}",candidateJobEntity.getId(),result.getBody());
        if (result.getStatusCode() == HttpStatus.OK) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public InterviewResultVO createInterview(InterviewRequestDTO interviewRequestDTO, JobCreateBO jobCreateBO) {
        HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeadersByJob(JobConvert.INSTANCE.toJob(jobCreateBO));
        HttpEntity<InterviewRequestDTO> requestEntity =
                new HttpEntity<>(interviewRequestDTO, headers);
        log.info("createInterviewWithQuestions interviewRequestDTO {}", JsonUtils.toJson(interviewRequestDTO));
        ResponseEntity<InterviewResultVO> result = restTemplate.postForEntity(aiInterviewConfig.getCreateInterviewWithQuestions(), requestEntity, InterviewResultVO.class);
        if (result.getStatusCode() == HttpStatus.OK) {
            log.info("createInterviewWithQuestions result:{}", result.getBody());
            return result.getBody();
        }
        return null;
    }

    @Override
    public InterviewUpdateResultVO updateInterview(InterviewUpdateRequestDTO interviewRequestDTO,JobEntity jobEntity ) {

        HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeadersByJob(jobEntity);
        HttpEntity<InterviewUpdateRequestDTO> requestEntity =
                new HttpEntity<>(interviewRequestDTO, headers);
        log.info("updateInterview interviewRequestDTO {}", interviewRequestDTO);
        ResponseEntity<InterviewUpdateResultVO> result = restTemplate.postForEntity(aiInterviewConfig.getUpdateInterviewDetails(), requestEntity, InterviewUpdateResultVO.class);
        if (result.getBody() != null){
            log.info("updateInterview result:interviewId{},result:{}",interviewRequestDTO.getInterviewId(),result.getBody());
            return result.getBody();
        }
        return null;
    }

    @Override
    public String getInterviewReportUrl(String callId) {
        return s3Utils.generatePresignedUrl(exportInterviewReport(callId));
    }


    @Override
    public JobResponseDataDTO generateJob(GenerateJobRequestDTO aiJobRequest) {
        // 如果jobTitle没有值 直接返回
        if(org.apache.commons.lang3.StringUtils.isBlank(aiJobRequest.getJobTitle())) {
            return new JobResponseDataDTO();
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(aiJobRequest.getExtraInfo())) {
            aiJobRequest.setExtraInfo(aiJobRequest.getJobTitle());
        }
        // ai 在生成job时候 国家需要使用英文名称
        LocationAIDTO location = aiJobRequest.getLocation();
        if (location != null) {
            Long countryId = location.getCountryId();
            Long stateId = location.getStateId();
            Long cityId = location.getCityId();
            CountryDTO country = CommonUtils.getSafeFirstFromList(locationService.listEnCountryByCountryIds(List.of(countryId)), new CountryDTO());
            StateDTO state = CommonUtils.getSafeFirstFromList(locationService.listEnStateByStateIds(List.of(stateId)), new StateDTO());
            CityDTO city = CommonUtils.getSafeFirstFromList(locationService.listEnCityByCityIds(List.of(cityId)), new CityDTO());
            location.setCountryName(CommonUtils.getSafeValue(country::getName, location.getCountryName()));
            location.setStateName(CommonUtils.getSafeValue(state::getName, location.getStateName()));
            location.setCityName(CommonUtils.getSafeValue(city::getName, location.getCityName()));
        }
        HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeaders();
        HttpEntity<GenerateJobRequestDTO> requestEntity =
                new HttpEntity<>(aiJobRequest, headers);
        GenerateJobResponseDTO result = restTemplate.postForObject(aiInterviewConfig.getGenerateJobDescription(), requestEntity, GenerateJobResponseDTO.class);
        if (Objects.nonNull(result) && result.getCode() == 0 && Objects.nonNull(result.getData())) {
            JobResponseDataDTO jobResponseDataDTO = result.getData();
            if (StringUtils.hasText(jobResponseDataDTO.getCurrencyType())) {
                jobResponseDataDTO.setSalaryType(12);//设置计薪类型为 时薪
                List<DictionaryEntity> dictionaryEntities = dictionaryService.list();
                Map<String, Map<String, DictionaryEntity>> dictionaryMap = dictionaryEntities.stream()
                        // 按type分组
                        .collect(Collectors.groupingBy(
                                DictionaryEntity::getType,
                                // 每组内再按value分组，保留对应的实体
                                Collectors.toMap(
                                        entity -> entity.getCode().toLowerCase(),
                                        entity -> entity,
                                        // 若存在相同value的实体，保留第一个
                                        (existing, replacement) -> existing
                                )
                        ));
                // 优化字典查询逻辑
                Optional.ofNullable(jobResponseDataDTO.getCurrencyType())
                        .map(String::toLowerCase)
                        .flatMap(lowercasecurrencyType ->
                                Optional.ofNullable(dictionaryMap.get(DictionaryEnum.REPORT.getName()))
                                        .filter(map -> !map.isEmpty())
                                        .map(map -> map.get(lowercasecurrencyType))
                        )
                        .filter(Objects::nonNull)
                        .map(DictionaryEntity::getId)
                        .ifPresent(jobResponseDataDTO::setCurrency);
            }
            return result.getData();
        }
        return null;
    }

    @Override
    public Boolean checkInfo(String candidateEmail, String interviewId, Long applicationId, String candidateName) {
        List<CandidateJobEntity> candidateJobEntitys = candidateJobService.checkInfo(candidateEmail, interviewId, applicationId, candidateName);
        if (CollectionUtils.isEmpty(candidateJobEntitys)) {
            log.error("No candidate job entities found for email: {} and interviewId: {} and applicationId: {} and candidateName: {}", candidateEmail, interviewId, applicationId, candidateName);
            return false;
        }

        // 获取具有最大interviewEndTime的CandidateJobEntity
        Optional<CandidateJobEntity> candidateJobWithMaxTime = candidateJobEntitys.stream()
                .filter(entity -> entity.getInterviewEndTime() != null)
                .max(Comparator.comparing(CandidateJobEntity::getInterviewEndTime));

        if (!candidateJobWithMaxTime.isPresent()) {
            log.error("No valid interview end time found for candidate: {}", candidateEmail);
            throw new BusinessException(GlobalStatusCode.FAIL,"No valid interview end time found for candidate.");
        }

        CandidateJobEntity candidateJob = candidateJobWithMaxTime.get();
        LocalDateTime maxInterviewEndTime = candidateJob.getInterviewEndTime();
        LocalDateTime now = LocalDateTime.now();

        log.info("Checking interview validity for candidate: {}, max end time: {}, current time: {}",
                candidateEmail, maxInterviewEndTime, now);

        // 判断是否已过期
        if (maxInterviewEndTime.isBefore(now)) {
            log.error("Interview has expired for candidate: {}, expired at: {}", candidateEmail, maxInterviewEndTime);
            throw new BusinessException(GlobalStatusCode.FAIL,"Interview has expired for candidate.");
        }

        log.info("Interview is valid for candidate: {}, expires at: {}", candidateEmail, maxInterviewEndTime);

        return true;
    }

    @Override
    public Boolean aiInterviewCallback(AiCallbackDTO aiCallbackDTO) {
        log.info("Ai interview result call back, event: {}, call-id: {}, application-id: {}",
                aiCallbackDTO.getEvent(), aiCallbackDTO.getData() == null ? aiCallbackDTO.getCallId() : aiCallbackDTO.getData().getCallId(),
                aiCallbackDTO.getData() == null ? "null" : aiCallbackDTO.getData().getApplicationId());
        if (AICallbackTypeEnum.PHONE_INTERVIEW_COMPLETED.getName().equals(aiCallbackDTO.getEvent()) || AICallbackTypeEnum.PHONE_INTERVIEW_NOT_COMPLETED.getName().equals(aiCallbackDTO.getEvent())) {
            log.info("PHONE_INTERVIEW_COMPLETED call id : {}", aiCallbackDTO.getCallId() != null ? aiCallbackDTO.getCallId() : "null");
            log.info("PHONE_INTERVIEW_COMPLETED data call id : {}", aiCallbackDTO.getData() != null ? aiCallbackDTO.getData().getCallId() : "null");
            log.info("PHONE_INTERVIEW_COMPLETED data schedule id : {}", aiCallbackDTO.getData() != null ? aiCallbackDTO.getData().getScheduleId() : "null");
            return aiPhoneInterviewCallback(aiCallbackDTO);
        }
        if (AICallbackTypeEnum.PHONE_INTERVIEW_NOTE_CONNECTED.getName().equals(aiCallbackDTO.getEvent())) {
            log.info("PHONE_INTERVIEW_NOTE_CONNECTED call id : {}", aiCallbackDTO.getCallId() != null ? aiCallbackDTO.getCallId() : "null");
            log.info("PHONE_INTERVIEW_NOTE_CONNECTED data call id : {}", aiCallbackDTO.getData() != null ? aiCallbackDTO.getData().getCallId() : "null");
            log.info("PHONE_INTERVIEW_NOTE_CONNECTED data schedule id : {}", aiCallbackDTO.getData() != null ? aiCallbackDTO.getData().getScheduleId() : "null");
            return sendMessageToCandidate(aiCallbackDTO);
        }
        if (AICallbackTypeEnum.PHONE_INTERVIEW_CANCELLED.getName().equals(aiCallbackDTO.getEvent())) {
            log.info("PHONE_INTERVIEW_NOTE_CONNECTED call id : {}", aiCallbackDTO.getCallId() != null ? aiCallbackDTO.getCallId() : "null");
            log.info("PHONE_INTERVIEW_NOTE_CONNECTED data call id : {}", aiCallbackDTO.getData() != null ? aiCallbackDTO.getData().getCallId() : "null");
            log.info("PHONE_INTERVIEW_NOTE_CONNECTED data schedule id : {}", aiCallbackDTO.getData() != null ? aiCallbackDTO.getData().getScheduleId() : "null");
            return cancelPhoneInterview(aiCallbackDTO);
        }
        if (AICallbackTypeEnum.PDF_REPORT.getName().equals(aiCallbackDTO.getEvent())) {
            LambdaQueryWrapper<AiCallbackEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AiCallbackEntity::getCallId, aiCallbackDTO.getCallId())
                    .eq(AiCallbackEntity::getEvent, AICallbackTypeEnum.PHONE_INTERVIEW_COMPLETED.name());
            AiCallbackEntity aiCallback = aiCallbackMapper.selectOne(queryWrapper);
            if (aiCallback != null) {
                AiVettedResultEntity aiVettedResult = aiVettedResultService.selectByCandidateJobId(aiCallback.getCandidateJobId());
                String interviewReportS3Key = aiInterviewConfig.getPdfReportPath().replace("{call_id}", aiCallbackDTO.getCallId());
                log.info("Processing interview report S3 key from AI callback: {}", interviewReportS3Key);
                aiVettedResult.setInterviewReportUrl(interviewReportS3Key);
                AiCallbackEntity reportCallback = AiCallbackConvert.INSTANCE.toAiCallbackEntity(aiCallbackDTO);
                reportCallback.setCandidateJobId(aiCallback.getCandidateJobId());
                reportCallback.setStatus(AICallbackStatusEnum.PR_D.getCode());
                LocalDateTime now = LocalDateTime.now();
                reportCallback.setCreateTime(now);
                reportCallback.setUpdateTime(now);
                aiCallbackService.saveReportCallback(reportCallback, aiVettedResult);

                // 异步发送面试报告邮件
                Long candidateJobId = aiVettedResult.getCandidateJobId();
                try {
                    aiTaskExecutor.execute(() -> {
                        try {
                            log.info("Sending interview report email for candidateJobId: {}", candidateJobId);
                            candidateJobDomainService.sendInterviewReportMailToCandidate(candidateJobId);
                        } catch (Exception e) {
                            log.error("Failed to send interview report email for candidateJobId: {}", candidateJobId, e);
                        }
                    });
                } catch (Exception e) {
                    log.error("Error in sending interview report to candidate, candidateJobId:{}", candidateJobId, e);
                }
                return true;
            }
        }
        return callback(aiCallbackDTO);
    }

    private Boolean sendMessageToCandidate(AiCallbackDTO dto) {
        if (dto.getData() == null) {
            log.warn("sendMessageToCandidate: data is null, event: {}", dto.getEvent());
            return false;
        }

        AiCallbackDataVO data = dto.getData();
        String applicationId = data.getApplicationId();
        String candidatePhone = data.getCandidatePhone();
        String fromNumber = data.getFromPhone();

        if (!StringUtils.hasText(applicationId)) {
            log.warn("sendMessageToCandidate: applicationId is empty, event: {}", dto.getEvent());
            return false;
        }

        if (!StringUtils.hasText(candidatePhone)) {
            log.warn("sendMessageToCandidate: candidatePhone is empty, applicationId: {}", applicationId);
            return false;
        }

        if (!StringUtils.hasText(fromNumber)) {
            log.warn("sendMessageToCandidate: fromNumber is empty, applicationId: {}", applicationId);
            return false;
        }

        try {
            // Get candidate job entity
            Long candidateJobId = Long.parseLong(applicationId);
            CandidateJobEntity candidateJobEntity = candidateJobService.getById(candidateJobId);
            if (candidateJobEntity == null) {
                log.warn("sendMessageToCandidate: candidateJobEntity not found, applicationId: {}", applicationId);
                return false;
            }

            // Get job entity
            JobEntity jobEntity = jobService.getById(candidateJobEntity.getJobId());
            if (jobEntity == null) {
                log.warn("sendMessageToCandidate: jobEntity not found, jobId: {}", candidateJobEntity.getJobId());
                return false;
            }

            // Get candidate entity
            CandidateEntity candidateEntity = candidateService.getById(candidateJobEntity.getCandidateId());
            if (candidateEntity == null) {
                log.warn("sendMessageToCandidate: candidateEntity not found, candidateId: {}", candidateJobEntity.getCandidateId());
                return false;
            }

            // 查询 ai_call_back 表，检查是否已有此事件的记录
            LambdaQueryWrapper<AiCallbackEntity> callbackQueryWrapper = new LambdaQueryWrapper<>();
            callbackQueryWrapper.eq(AiCallbackEntity::getCandidateJobId, candidateJobId)
                                .eq(AiCallbackEntity::getEvent, AICallbackTypeEnum.PHONE_INTERVIEW_NOTE_CONNECTED.getName());
            AiCallbackEntity existingCallback = aiCallbackMapper.selectOne(callbackQueryWrapper);

            if (existingCallback != null) {
                //存在记录，证明候选人第二次未接电话面试，视为自动放弃
                log.info("sendMessageToCandidate: event already exists for candidateJobId: {}, event: {}", candidateJobId, AICallbackTypeEnum.PHONE_INTERVIEW_NOTE_CONNECTED.getName());
                candidateJobEntity.setInterviewPhoneStatus(
                        InterviewPhoneStatusEnum.change(candidateJobEntity.getInterviewPhoneStatus(), false).getCode()
                );
                AiCallbackEntity callback = buildAiCallback(candidateJobId, dto, LocalDateTime.now());

                aiCallbackService.candidateRejectPhoneInterview(candidateJobId, candidateJobEntity, callback);
                return true;
            }

            CandidateRecallEntity candidateRecall = candidateRecallService.getByCandidateJobId(candidateJobId);
            if (candidateRecall != null && SmsSendStatusEnum.SUCCESS.getCode() == candidateRecall.getSmsStatus()) {
                log.info("sendMessageToCandidate: system already send message to candidate for candidateJobId: {}, event: {}", candidateJobId, AICallbackTypeEnum.PHONE_INTERVIEW_NOTE_CONNECTED.getName());
                return true;
            }
            try {
                //不存在记录，证明候选人第一次未接电话面试，做轮询准备
                log.info("sendMessageToCandidate: event not exists for candidateJobId: {}, event: {}", candidateJobId, AICallbackTypeEnum.PHONE_INTERVIEW_NOTE_CONNECTED.getName());
                candidateJobEntity.setInterviewPhoneStatus(
                        InterviewPhoneStatusEnum.change(candidateJobEntity.getInterviewPhoneStatus(), false).getCode()
                );
                AiCallbackEntity callback = buildAiCallback(candidateJobId, dto, LocalDateTime.now());

                aiCallbackService.recallCandidateInterviewPhone(candidateJobId, candidateJobEntity, data, callback);
            } catch (Exception e) {
                log.error("Event of phone interview not connected save to call back failure, application id:{}.", applicationId, e);
            }

            // Get company name
            String companyName = "";
            if (StringUtils.hasText(candidateJobEntity.getCompanyCode())) {
                CompanyInfoSimpleDTO companyInfo = companyService.getCompanyInfoByCode(candidateJobEntity.getCompanyCode());
                if (companyInfo != null && StringUtils.hasText(companyInfo.getName())) {
                    companyName = companyInfo.getName();
                }
            }
            String candidateName = candidateEntity.getCandidateName();
            String jobTitle = jobEntity.getTitle();
            // Determine language based on job location
            boolean isChinese = isJobLocationInChina(candidateJobEntity);
            // Build SMS message body
            String smsBody = buildSmsMessage(isChinese, candidateName, companyName, jobTitle, fromNumber);
            // Send SMS
            SendSmsRequestDTO smsRequest = SendSmsRequestDTO.builder()
                    .from(aiInterviewConfig.getSendSmsPhone())
                    .to(candidatePhone)
                    .body(smsBody)
                    .build();

            HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeadersByJob(jobEntity);
            HttpEntity<SendSmsRequestDTO> requestEntity = new HttpEntity<>(smsRequest, headers);

            log.info("Sending SMS to candidate, phone: {}, applicationId: {}", candidatePhone, applicationId);
            ResponseEntity<SendSmsResponseVO> response = restTemplate.postForEntity(
                    aiInterviewConfig.getSendSmsUrl(),
                    requestEntity,
                    SendSmsResponseVO.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                SendSmsResponseVO smsResponse = response.getBody();
                if (Boolean.TRUE.equals(smsResponse.getSuccess())) {
                    log.info("SMS sent successfully, messageSid: {}, to: {}", smsResponse.getMessageSid(), smsResponse.getTo());
                    candidateRecallService.updateSmsStatusByCandidateJobId(candidateJobId, SmsSendStatusEnum.SUCCESS.getCode());
                    return true;
                } else {
                    log.warn("SMS sending failed, status: {}, to: {}", smsResponse.getStatus(), smsResponse.getTo());
                    return false;
                }
            } else {
                log.warn("SMS sending failed, status code: {}", response.getStatusCode());
                return false;
            }
        } catch (NumberFormatException e) {
            log.error("sendMessageToCandidate: invalid applicationId format, applicationId: {}", applicationId, e);
            return false;
        } catch (Exception e) {
            log.error("sendMessageToCandidate: failed to send SMS, applicationId: {}", applicationId, e);
            return false;
        }
    }

    private Boolean cancelPhoneInterview(AiCallbackDTO dto) {
        if (dto.getData() == null) {
            log.warn("cancelPhoneInterview: data is null, event: {}", dto.getEvent());
            return false;
        }

        AiCallbackDataVO data = dto.getData();
        String applicationId = data.getApplicationId();

        if (!StringUtils.hasText(applicationId)) {
            log.warn("cancelPhoneInterview: applicationId is empty, event: {}", dto.getEvent());
            return false;
        }

        try {
            Long candidateJobId = Long.parseLong(applicationId);
            CandidateJobEntity candidateJobEntity = candidateJobService.getById(candidateJobId);
            if (candidateJobEntity == null) {
                log.warn("cancelPhoneInterview: candidateJobEntity not found, applicationId: {}", applicationId);
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            
            // 构建回调实体
            AiCallbackEntity callback = buildAiCallback(candidateJobId, dto, now);

            // 使用事务方法保存回调并删除回拨记录
            aiCallbackService.cancelPhoneInterview(callback, candidateJobId);

            // 调用 jobFlowService.fireEvent 拒绝候选人
            jobFlowService.fireEvent(candidateJobId, JobApplyStatusEvent.REJECT);
            log.info("cancelPhoneInterview: fired REJECT event, candidateJobId: {}", candidateJobId);

            return true;
        } catch (NumberFormatException e) {
            log.error("cancelPhoneInterview: invalid applicationId format, applicationId: {}", applicationId, e);
            return false;
        } catch (Exception e) {
            log.error("cancelPhoneInterview: failed to process, applicationId: {}", applicationId, e);
            return false;
        }
    }

    /**
     * Build SMS message body based on language
     */
    private String buildSmsMessage(boolean isChinese, String candidateName, String companyName, String jobTitle, String phoneNumber) throws IOException {
        try {
            // Load template from file
            String templatePath = isChinese
                    ? "templates/sms-interview-followup-zh.txt"
                    : "templates/sms-interview-followup-en.txt";

            ClassPathResource resource = new ClassPathResource(templatePath);
            String template;
            try (InputStream inputStream = resource.getInputStream()) {
                template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Replace placeholders
            String genderTitle = isChinese ? "先生/女士" : "Mr./Ms.";
            template = template.replace("{candidateName}", candidateName);
            template = template.replace("{genderTitle}", genderTitle);
            template = template.replace("{companyName}", companyName);
            template = template.replace("{jobTitle}", jobTitle);
            template = template.replace("{phoneNumber}", phoneNumber);

            return template;
        } catch (Exception e) {
            log.error("Failed to load SMS template, using fallback template. isChinese: {}", isChinese, e);
            throw e;
        }
    }

    /**
     * Check if job location is in China
     */
    private boolean isJobLocationInChina(CandidateJobEntity candidateJobEntity) {
        Long jobCountryId = candidateJobEntity.getJobCountryId();
        if (jobCountryId == null) {
            log.debug("Job countryId is null, default to English template");
            return false;
        }

        try {
            CountryDTO countryDTO = locationService.getCountryById(jobCountryId);
            if (countryDTO == null) {
                log.warn("Country not found for countryId: {}, default to English template", jobCountryId);
                return false;
            }

            String isoCode = countryDTO.getIsoCode();
            String countryName = countryDTO.getName();

            boolean isChina = "CN".equalsIgnoreCase(isoCode)
                    || "China".equalsIgnoreCase(countryName)
                    || "中国".equals(countryName);

            log.debug("Job location check: countryId={}, isoCode={}, countryName={}, isChina={}",
                    jobCountryId, isoCode, countryName, isChina);

            return isChina;
        } catch (Exception e) {
            log.error("Error checking job location for countryId: {}, default to English template", jobCountryId, e);
            return false;
        }
    }

    private Boolean aiPhoneInterviewCallback(AiCallbackDTO aiCallbackDTO) {
        log.info("Ai phone interview result call back, call-id: {}, application-id: {}",
                aiCallbackDTO.getData().getCallId(), aiCallbackDTO.getData().getApplicationId());
        log.info("Ai phone, application-id:{}, analytics:{}", aiCallbackDTO.getCallId(), aiCallbackDTO.getData().getAnalytics());
        if (aiCallbackDTO.getData() == null) {
            return false;
        }
        AiCallbackDataVO aiCallbackData = aiCallbackDTO.getData();
        LambdaQueryWrapper<AiCallbackEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiCallbackEntity::getEvent, aiCallbackDTO.getEvent());
        wrapper.eq(AiCallbackEntity::getCallId, aiCallbackData.getCallId());
        wrapper.orderByDesc(AiCallbackEntity::getCreateTime);
        wrapper.last("limit 1");
        AiCallbackEntity aiCallback = aiCallbackMapper.selectOne(wrapper);
        //已经存在则不需要处理，证明是重复调用
        if (aiCallback != null && AICallbackTypeEnum.PHONE_INTERVIEW_COMPLETED.getName().equals(aiCallbackDTO.getEvent())) {
            log.info("Ai phone interview result call back, already exist, event: {}, callId: {}", aiCallbackDTO.getEvent(), aiCallbackData.getCallId());
            return false;
        }

        LambdaQueryWrapper<JobEntity> jobWrapper = new LambdaQueryWrapper<>();
        jobWrapper.eq(JobEntity::getInterviewUrlId, aiCallbackData.getInterviewId());
        JobEntity job = jobService.getOne(jobWrapper);
        //没有岗位则直接结束
        if (job == null) {
            log.info("Ai phone interview result call back, job not exist, interviewId: {}", aiCallbackData.getInterviewId());
            return false;
        }

        CandidateJobEntity candidateJobEntity = candidateJobService.getById(aiCallbackData.getApplicationId());
        if (candidateJobEntity == null) {
            log.info("Ai phone interview result call back, candidate job not exist, jobId: {}, candidatePhone: {}", job.getId(), aiCallbackData.getCandidatePhone());
            return false;
        }

        Long candidateJobId = candidateJobEntity.getId();
        LocalDateTime now = LocalDateTime.now();

        aiCallback = buildAiCallback(candidateJobId, aiCallbackDTO, now);
        candidateJobEntity = fillCandidateJob(candidateJobEntity, aiCallbackData);
        AiVettedResultEntity aiVettedResult = buildAiVettedResult(candidateJobId, aiCallbackData, now);
        List<AiVettedResultSkillEntity> aiVettedResultSkills = buildAiVettedSkills(aiCallbackData.getAnalytics(), now);

        CallVO callVO = buildCallVO(aiCallbackData);

        if (AICallbackTypeEnum.PHONE_INTERVIEW_COMPLETED.getName().equals(aiCallbackDTO.getEvent())) {
            aiCallbackService.saveAiInterviewResult(candidateJobEntity, aiCallback, aiVettedResult, aiVettedResultSkills);
            log.info("Ai phone interview result call back, save success, candidateJobId: {}", candidateJobId);
            try {
                Long jobCountryId = candidateJobEntity.getJobCountryId();
                aiTaskExecutor.execute(()->{
                    CountryDTO countryDTO = locationService.getCountryById(jobCountryId);
                    HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeadersByJob(job);
                    HttpEntity<?> requestEntity =
                            new HttpEntity<>(headers);
                    String result = restTemplate.postForObject(aiInterviewConfig.getProcessPhoneInterviewReport() + "?return_html=false&call_id=" + aiCallbackData.getCallId() +"&location="+countryDTO.getName()
                            ,requestEntity,String.class);
                    log.info("PDF generation task submitted:{},call_id:{}",result,aiCallbackData.getCallId());
                });
            } catch (Exception ex) {
                log.error("Error in processing interview report: {},call_id:{}",ex.getMessage(),aiCallbackDTO.getCallId());
            }
            jobFlowService.fireEvent(candidateJobId, JobApplyStatusEvent.VETTED);
        } else {
            aiCallbackService.saveAiInterviewIncompleteResult(aiCallbackDTO.getData().getScheduleId(), candidateJobEntity, aiCallback, aiVettedResult, aiVettedResultSkills);
            log.info("Ai phone call interview incomplete, callVO:{}, candidateJobId:{}, callId:{}", callVO, candidateJobId, aiCallbackData.getCallId());
            return true;
        }

        log.info("Ai phone call confirmInterviewFreeze, callVO:{}, candidateJobId:{}, callId:{}", callVO, candidateJobId, aiCallbackData.getCallId());
        confirmInterviewFreeze(callVO, candidateJobId, aiCallbackData.getCallId());
        return true;
    }

    @Override
    public Boolean callback(AiCallbackDTO aiCallbackDTO) {
        log.info("callback aiCallbackDTO:{}",aiCallbackDTO);
        LambdaQueryWrapper<AiCallbackEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiCallbackEntity::getEvent,aiCallbackDTO.getEvent());
        wrapper.eq(AiCallbackEntity::getCallId,aiCallbackDTO.getCallId());
        wrapper.orderByDesc(AiCallbackEntity::getCreateTime);
        wrapper.last("limit 1");
        AiCallbackEntity aiCallbackEntity = aiCallbackMapper.selectOne(wrapper);
        if (Objects.isNull(aiCallbackEntity)) {
            aiCallbackEntity = AiCallbackConvert.INSTANCE.toAiCallbackEntity(aiCallbackDTO);
            aiCallbackEntity.setCreateTime(LocalDateTime.now());
        }
        aiCallbackEntity.setUpdateTime(LocalDateTime.now());
        aiCallbackEntity.setStatus(AICallbackStatusEnum.UN_PR.getCode());
        LambdaQueryWrapper<JobEntity> jobWrapper = new LambdaQueryWrapper<>();
        jobWrapper.eq(JobEntity::getInterviewUrlId,aiCallbackDTO.getInterviewId());
        JobEntity jobEntity = jobService.getOne(jobWrapper);
        if (jobEntity == null){
            return true;
        }
        LambdaQueryWrapper<CandidateEntity> candidateWrapper = new LambdaQueryWrapper<>();
        candidateWrapper.eq(CandidateEntity::getCandidateEmail,aiCallbackDTO.getInterviewerEmail());
        List<CandidateEntity> candidateEntitys = candidateService.list(candidateWrapper);
        if (candidateEntitys == null || candidateEntitys.isEmpty()){
            return true;
        }
        // 优先使用从AI回调中传递的application_id来确定具体的应聘者账号
        Long candidateJobId = null;
        if (aiCallbackDTO.getCandidateJobId() != null) {
            // 如果AI传递了application_id，直接使用
            candidateJobId = aiCallbackDTO.getCandidateJobId();
            log.info("Using application_id from AI callback: {}", candidateJobId);
        } else {
            // 如果没有传递application_id，回退到原有逻辑
            LambdaQueryWrapper<AiCallbackEntity> aiCallbackQueryWrapper = new LambdaQueryWrapper<>();
            aiCallbackQueryWrapper.eq(AiCallbackEntity::getCallId,aiCallbackDTO.getCallId());
            aiCallbackQueryWrapper.last("limit 1");
            AiCallbackEntity aiCallback = aiCallbackMapper.selectOne(aiCallbackQueryWrapper);
            if (Objects.isNull(aiCallback)){
                LambdaQueryWrapper<CandidateJobEntity> candidateJobWrapper = new LambdaQueryWrapper<>();
                candidateJobWrapper.eq(CandidateJobEntity::getJobId,jobEntity.getId());
                candidateJobWrapper.eq(CandidateJobEntity::getApplyStatus, JobApplyStatus.SCREENED.getCode());
                candidateJobWrapper.in(CandidateJobEntity::getCandidateId,candidateEntitys.stream().map(CandidateEntity::getId).collect(Collectors.toList()));
                candidateJobWrapper.last("limit 1");
                CandidateJobEntity candidateJobEntity = candidateJobService.getOne(candidateJobWrapper);
                if (Objects.isNull(candidateJobEntity)){
                    log.error("candidateJobEntity is null,jobId:{}",jobEntity.getId());
                    return true;
                }
                candidateJobId = candidateJobEntity.getId();
                log.info("Using fallback logic to find candidateJobId: {}", candidateJobId);
            } else {
                candidateJobId = aiCallback.getCandidateJobId();
                log.info("Using existing aiCallback candidateJobId: {}", candidateJobId);
            }
        }
        aiCallbackEntity.setCandidateJobId(candidateJobId);
        boolean isSuccess = aiCallbackMapper.insertOrUpdate(aiCallbackEntity);
        AiCallbackDTO  finalAiCallbackDTO = AiCallbackConvert.INSTANCE.toAiCallbackDTO(aiCallbackEntity);
        aiTaskExecutor.execute(() ->
                this.getInterviewResult(finalAiCallbackDTO,jobEntity)
        );
        return isSuccess;
    }

    @Override
    public Boolean getInterviewResult(AiCallbackDTO aiCallbackDTO,JobEntity jobEntity) {
        AiCallbackEntity aiCallbackEntity = AiCallbackConvert.INSTANCE.toAiCallbackEntity(aiCallbackDTO);
        aiCallbackEntity.setStatus(AICallbackStatusEnum.RP_I.getCode());
        aiCallbackMapper.updateById(aiCallbackEntity);
        List<String> events = new ArrayList<>(List.of(AICallbackTypeEnum.REPORT.getName(), AICallbackTypeEnum.CAMER_RECORDING_URL.getName()));
        //获取实际面试类型
        Integer interviewType=getInterviewType(aiCallbackDTO.getCallId());
        if (Objects.nonNull(interviewType) && InterviewTypeEnum.VIDEO.getCode()==interviewType) {
            events.add(AICallbackTypeEnum.CHEATING_ANALYSIS_COMPLETED.getName());
        }
        if (events.contains(aiCallbackDTO.getEvent())){
            LambdaQueryWrapper<AiCallbackEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AiCallbackEntity::getCallId,aiCallbackDTO.getCallId());
            List<AiCallbackEntity> aiCallbackEntities = aiCallbackMapper.selectList(queryWrapper);
            // 判断aiCallbackEntities里是否包含events所有值
            List<String> existingEvents = aiCallbackEntities.stream()
                    .map(AiCallbackEntity::getEvent)
                    .collect(Collectors.toList());
            boolean containsAllEvents = existingEvents.containsAll(events);
            if (containsAllEvents){
                log.info("Existing events: {}, Required events: {}, Contains all: {}", existingEvents, events, containsAllEvents);
                try {
                    aiTaskExecutor.execute(()->{
                        CandidateJobEntity candidateJobEntity = candidateJobService.getById(aiCallbackDTO.getCandidateJobId());
                        CountryDTO countryDTO = locationService.getCountryById(candidateJobEntity.getJobCountryId());
                        HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeadersByJob(jobEntity);
                        HttpEntity<?> requestEntity =
                                new HttpEntity<>(headers);
                        String result = restTemplate.postForObject(aiInterviewConfig.getProcessInterviewReport() + "?return_html=false&call_id=" + aiCallbackDTO.getCallId()+"&location="+countryDTO.getName()
                                ,requestEntity,String.class);
                        log.info("PDF generation task submitted:{},call_id:{}",result,aiCallbackDTO.getCallId());
                    });
                } catch (Exception e) {
                    log.error("Error in processing interview report: {},call_id:{}",e.getMessage(),aiCallbackDTO.getCallId());
                }
            }
        }

        try {
            log.info("getInterviewResult params:{}",aiCallbackDTO);
            
            //获取岗位申请信息
            CandidateJobEntity candidateJobEntity = new CandidateJobEntity();
            candidateJobEntity.setId(aiCallbackDTO.getCandidateJobId());
            //获取面试结果信息
            AiVettedResultEntity aiVettedResultEntity = new  AiVettedResultEntity();
            aiVettedResultEntity.setCandidateJobId(aiCallbackDTO.getCandidateJobId());
            aiVettedResultEntity.setId(aiCallbackDTO.getCandidateJobId());
            aiVettedResultEntity.setInterviewType(interviewType);
            // 处理PDF_REPORT事件 - 不调用getCall接口
            if (aiCallbackEntity.getEvent().equals(AICallbackTypeEnum.PDF_REPORT.getName())) {
                String interviewReportS3Key = aiInterviewConfig.getPdfReportPath().replace("{call_id}", aiCallbackDTO.getCallId());
                log.info("Processing interview report S3 key from AI callback: {}", interviewReportS3Key);
                aiVettedResultEntity.setInterviewReportUrl(interviewReportS3Key);
                Integer totalScore = calculateTotalScore(jobEntity, aiVettedResultEntity);
                aiVettedResultEntity.setOverallScore(totalScore);
                aiVettedResultService.saveOrUpdate(aiVettedResultEntity);
                aiCallbackEntity.setStatus(AICallbackStatusEnum.PR_D.getCode());
                aiCallbackMapper.updateById(aiCallbackEntity);

                // 异步发送面试报告邮件
                Long candidateJobId = aiCallbackDTO.getCandidateJobId();
                try {
                    aiTaskExecutor.execute(() -> {
                        try {
                            log.info("Sending interview report email for candidateJobId: {}", candidateJobId);
                            candidateJobDomainService.sendInterviewReportMailToCandidate(candidateJobId);
                        } catch (Exception e) {
                            log.error("Failed to send interview report email for candidateJobId: {}", candidateJobId, e);
                        }
                    });
                } catch (Exception e) {
                    log.error("Error in sending interview report to candidate, candidateJobId:{}", candidateJobId, e);
                }
                //智能评估
                handlerVettedIntelligence(aiVettedResultEntity.getCandidateJobId(),jobEntity.getId());
                return true;
            }

            // 其他事件类型需要调用getCall接口
            Map<String, String> params = new HashMap<>();
            params.put("id", aiCallbackDTO.getCallId());
            CallVO callVO = restTemplate.postForObject(aiInterviewConfig.getGetCall(), params, CallVO.class);
            if (callVO != null) {
                if (aiCallbackEntity.getEvent().equals(AICallbackTypeEnum.REPORT.getName())) {
                    aiVettedResultEntity.setInterviewTime(aiCallbackDTO.getTimestamp());
                    if (Objects.nonNull(callVO.getAnalytics())) {
                        aiVettedResultEntity.setInterviewScore(callVO.getAnalytics().getOverallScore());
                        candidateJobEntity.setOverallScore(callVO.getAnalytics().getOverallScore());
                        aiVettedResultEntity.setOverallSkillAssessment(callVO.getAnalytics().getOverallFeedback());
                    }
                    if (Objects.nonNull(callVO.getCallResponse())) {
                        aiVettedResultEntity.setTranscript(transcriptConverter.convertToJson(callVO.getCallResponse().getTranscript()));
                    }
                    candidateJobEntity.setInterviewTime(aiCallbackDTO.getTimestamp());
                }
                if (aiCallbackEntity.getEvent().equals(AICallbackTypeEnum.CHEATING_ANALYSIS_COMPLETED.getName()) && callVO.getCheatingAnalysis() !=null && callVO.getCheatingAnalysis().getProbability() != null) {
                    log.info("Process the content of cheating analysis. cheatingAnalysis:{}",callVO.getCheatingAnalysis().getProbability());
                    int proctoringScore = Math.round(((1-callVO.getCheatingAnalysis().getProbability()) * 100));
                    candidateJobEntity.setCheatingAnalysis(proctoringScore);
                    aiVettedResultEntity.setProctoringScore(proctoringScore);
                }
                if (aiCallbackEntity.getEvent().equals(AICallbackTypeEnum.CAMER_RECORDING_URL.getName()) && StringUtils.hasText(callVO.getCameraRecordingUrl())) {
                    log.info("Process the content of camera recording url:{}. ",callVO.getCameraRecordingUrl());
                    candidateJobEntity.setCameraRecordingUrl(callVO.getCameraRecordingUrl());
                    aiVettedResultEntity.setCameraRecordingUrl(callVO.getCameraRecordingUrl());
                }
                if (aiCallbackEntity.getEvent().equals(AICallbackTypeEnum.SUMMARIZED_VIDEO_URL_UPDATED.getName()) && StringUtils.hasText(callVO.getSummarizedVideoUrl())) {
                    log.info("Process the content of summarized video url:{}. ",callVO.getSummarizedVideoUrl());
                    aiVettedResultEntity.setSummarizedVideoUrl(callVO.getSummarizedVideoUrl());
                }
                //更新面试结果
                candidateJobService.updateById(candidateJobEntity);
                aiVettedResultService.saveOrUpdate(aiVettedResultEntity);
                if (aiCallbackEntity.getEvent().equals(AICallbackTypeEnum.REPORT.getName())) {
                    if (!interviewAnalysisCompletedSkill(aiVettedResultEntity,callVO)){
                        aiCallbackEntity.setStatus(AICallbackStatusEnum.PR_FAILED.getCode());
                        aiCallbackMapper.updateById(AiCallbackConvert.INSTANCE.toAiCallbackEntity(aiCallbackDTO));
                        return false;
                    }
                }
                aiCallbackEntity.setStatus(AICallbackStatusEnum.PR_D.getCode());
                aiCallbackMapper.updateById(aiCallbackEntity);
                jobFlowService.fireEvent(candidateJobEntity.getId(), JobApplyStatusEvent.VETTED);
                //面试积分扣除
                confirmInterviewFreeze(callVO,candidateJobEntity.getId(),aiCallbackDTO.getCallId());
                return true;
            }
        } catch (Exception e) {
            log.error("getInterviewResult params:{}, failed",aiCallbackDTO,e);
            aiCallbackEntity.setStatus(AICallbackStatusEnum.PR_FAILED.getCode());
            aiCallbackMapper.updateById(aiCallbackEntity);
        }
        log.info("AI did not return any data.");
        return false;
    }


    /**
     * 智能评估处理 - 根据候选人面试结果和岗位智能评分规则自动判断是否通过
     *
     * <p>该方法执行以下操作：</p>
     * <ul>
     *   <li>1. 获取候选人的AI面试结果和岗位的智能评分规则</li>
     *   <li>2. 对五个维度进行评分比对：面试表现、监考合规、技术能力、软技能、综合评分</li>
     *   <li>3. 判断候选人是否通过所有维度的阈值要求</li>
     *   <li>4. 更新ES中的评分规则数据</li>
     *   <li>5. 根据评估结果触发相应的工作流事件（自动通过或人工审核）</li>
     * </ul>
     *
     * @param candidateJobId 候选人岗位申请ID
     * @param jobId 岗位ID
     */
    private void handlerVettedIntelligence(Long candidateJobId, Long jobId) {
        // ==================== 1. 数据获取与验证 ====================
        // 获取候选人的AI面试评估结果
        AiVettedResultEntity aiVettedResult = aiVettedResultService.selectByCandidateJobId(candidateJobId);
        // 获取岗位的智能评分规则配置
        List<JobEsEntity> jobEsEntities = jobEsService.listJobIntelligenceScoreRuleByIds(Collections.singletonList(jobId));
        // 如果数据不存在，直接返回，不进行评估
        if (aiVettedResult == null || CollectionUtils.isEmpty(jobEsEntities)) {
            return;
        }
        JobEsEntity job = jobEsEntities.getFirst();
        // 检查智能评估开关是否开启
        if (job == null || job.getIntelligenceSwitch() == null || !job.getIntelligenceSwitch()) {
            return;
        }
        // ==================== 2. 评分规则组织 ====================
        List<IntelligenceScoreRuleDTO> scoreRules = job.getScoreRules();
        if (CollectionUtils.isEmpty(scoreRules)) {
            return;
        }

        // 将评分规则列表转换为Map，以子阶段代码为key，便于快速查找
        // 如果存在重复的子阶段代码，保留后者
        Map<String, IntelligenceScoreRuleDTO> subStageCodeMap = scoreRules.stream()
                .collect(Collectors.toMap(IntelligenceScoreRuleDTO::getSubStageCode, Function.identity(), (v1, v2) -> v2));

        // 提取五个评估维度的评分规则
        IntelligenceScoreRuleDTO interviewRule = subStageCodeMap.get(JobIntelligenceSubStageEnum.INTERVIEW.getCode());
        IntelligenceScoreRuleDTO proctoringRule = subStageCodeMap.get(JobIntelligenceSubStageEnum.PROCTORING.getCode());
        IntelligenceScoreRuleDTO technicalRule = subStageCodeMap.get(JobIntelligenceSubStageEnum.TECHNICAL_SKILLS.getCode());
        IntelligenceScoreRuleDTO softSkillRule = subStageCodeMap.get(JobIntelligenceSubStageEnum.SOFT_SKILLS.getCode());
        IntelligenceScoreRuleDTO overallRule = subStageCodeMap.get(JobIntelligenceSubStageEnum.OVERALL.getCode());

        // ==================== 3. 各维度评分评估 ====================
        // 评估面试表现维度：候选人的面试得分是否达到阈值
        boolean interviewPassed = evaluateDimension(aiVettedResult.getInterviewScore(), interviewRule);
        // 评估监考合规维度：候选人的监考得分是否达到阈值
        boolean proctoringPassed = evaluateDimension(aiVettedResult.getProctoringScore(), proctoringRule);
        // 评估技术能力维度：候选人的技术技能得分是否达到阈值
        boolean technicalSkillsPassed = evaluateDimension(aiVettedResult.getTechnicalSkillScore(), technicalRule);
        // 评估软技能维度：候选人的软技能得分是否达到阈值
        boolean softSkillsPassed = evaluateDimension(aiVettedResult.getSoftSkillScore(), softSkillRule);
        // 评估综合得分维度：候选人的综合得分是否达到阈值
        boolean overallScorePassed = evaluateDimension(aiVettedResult.getOverallScore(), overallRule);

        // ==================== 4. 自动通过判定 ====================
        // 只有当所有五个维度都通过时，才判定为自动通过
        boolean allDimensionsPassed = interviewPassed && proctoringPassed && technicalSkillsPassed
                && softSkillsPassed && overallScorePassed;

        log.info("智能评估结果: candidateJobId={}, 面试={}, 监考={}, 技术={}, 软技能={}, 综合={}, 最终结果={}",
                candidateJobId, interviewPassed, proctoringPassed, technicalSkillsPassed,
                softSkillsPassed, overallScorePassed, allDimensionsPassed ? "自动通过" : "人工审核");

        // ==================== 5. 更新ES评分规则数据 ====================
        // 将评分规则持久化到Elasticsearch，用于后续查询和报表
        Map<String, Object> fieldsValues = new HashMap<>();
        JobMatchResultVO jobMatchResultVO = resumeEsService.getMatchResultById(String.valueOf(aiVettedResult.getCandidateJobId()));
        List<IntelligenceScoreRuleDTO> scoreRuleDTOS =
                new ArrayList<>(Stream.of(interviewRule, proctoringRule, technicalRule, softSkillRule, overallRule)
                        .filter(Objects::nonNull)
                        .toList());
        if (CollectionUtils.isNotEmpty(jobMatchResultVO.getScoreRules())){
            scoreRuleDTOS.addAll(jobMatchResultVO.getScoreRules());
        }
        fieldsValues.put(LambdaUtil.getFieldName(JobMatchResultVO::getScoreRules), scoreRuleDTOS);
        resumeEsService.updateMatchEsFieldValue(String.valueOf(aiVettedResult.getCandidateJobId()), fieldsValues);

        // ==================== 6. 触发工作流状态转换 ====================
        if (allDimensionsPassed) {
            // 所有维度通过 -> 自动进入准备阶段
            log.info("触发工作流事件: candidateJobId={}, event=AUTO_AI_TO_READY", candidateJobId);
            jobFlowService.fireEvent(aiVettedResult.getCandidateJobId(), JobApplyStatusEvent.AUTO_AI_TO_READY);
        } else {
            // 至少一个维度未通过 -> 进入人工审核阶段
            log.info("触发工作流事件: candidateJobId={}, event=AUTO_AI_TO_REVIEW", candidateJobId);
            jobFlowService.fireEvent(aiVettedResult.getCandidateJobId(), JobApplyStatusEvent.AUTO_AI_TO_REVIEW);
        }
    }

    /**
     * 评估单个维度是否通过阈值
     *
     * @param candidateScore 候选人在该维度的得分（可能为null）
     * @param scoreRule 该维度的评分规则（可能为null）
     * @return true表示该维度通过，false表示未通过
     */
    private boolean evaluateDimension(Integer candidateScore, IntelligenceScoreRuleDTO scoreRule) {
        // 如果评分规则不存在，视为该维度未配置，返回false
        if (scoreRule == null) {
            return false;
        }

        // 如果候选人得分为null，视为该维度未通过，返回false
        if (candidateScore == null) {
            return false;
        }

        // 比较候选人得分与阈值：得分 >= 阈值时通过
        return candidateScore >= scoreRule.getThreshold();
    }


    /**
     * 确认面试积分扣除
     * @param callVO
     */
    private void confirmInterviewFreeze(CallVO callVO,Long candidateJobId,String callId){
        CandidateJobEntity candidateJobEntity = candidateJobService.getById(candidateJobId);
        log.info("confirmInterviewFreeze candidateJobId:{}",candidateJobEntity.getId());
        //积分消费模式
        int pricingModel = pointService.getPricingModel(candidateJobEntity.getCompanyCode());
        log.info("confirmInterviewFreeze candidateJobId:{},pricingModel:{}",candidateJobEntity.getId(),pricingModel);
        //冻结记录
        PointsOperationLog pointsOperationLog = pointsOperationLogService.selectinterviewFreezeLog(candidateJobEntity.getId());
        //判断是否有冻结记录
        if (pointsOperationLog!=null){
            log.info("confirmInterviewFreeze pointsOperationLog:{}",pointsOperationLog);
            if (pricingModel== PricingModelEnum.TOKEN.getCode()){
                pointService.confirmInterviewFreeze(candidateJobEntity.getId());
            }else if (pricingModel== PricingModelEnum.MINUTE.getCode()){
                CallResponseVO callResponse = callVO.getCallResponse();
                if (callResponse!=null){
                    CallCostVO callCost = callResponse.getCallCost();
                    if (callCost!=null){
                        JobEntity job = jobService.getById(candidateJobEntity.getJobId());
                        //获取实际面试类型
                        Integer interviewType=getInterviewType(callId);
                        if (interviewType==null){
                            interviewType=pointsOperationLog.getFreezeInterviewType();
                        }
                        //面试时长
                        int totalDurationSeconds = callCost.getTotalDurationSeconds();
                        log.info("confirmInterviewFreeze candidateJobId:{},pricingModel:{},totalDurationSeconds:{}",candidateJobEntity.getId(),pricingModel,totalDurationSeconds);
                        int totalMinutes = (totalDurationSeconds + 59) / 60;
                        //面试积分
                        Integer points=null;
                        if (interviewType!=null && interviewType== InterviewTypeEnum.AUDIO.getCode()){
                            points=CommonUtils.getInterviewFreezePoints(minuteBasedConfig.getCentExchangeRate(),totalMinutes,minuteBasedConfig.getAudioInterviewCostPerMinute());
                        }else{
                            points=CommonUtils.getInterviewFreezePoints(minuteBasedConfig.getCentExchangeRate(),totalMinutes,minuteBasedConfig.getInterviewCostPerMinute());
                        }
                        //先取消冻结
                        pointService.cancelInterviewFreeze(candidateJobEntity.getId());
                        //按照面试时长进行扣除积分
                        PointLogVO pointLog = new PointLogVO();
                        pointLog.setUserId(pointsOperationLog.getUserId());
                        pointLog.setOperUserId(pointsOperationLog.getOperUserId());
                        pointLog.setCompanyCode(job.getCompanyCode());
                        pointLog.setCandidateJobId(candidateJobEntity.getId());
                        pointLog.setJobId(candidateJobEntity.getJobId());
                        pointLog.setCandidateId(candidateJobEntity.getCandidateId());
                        pointLog.setTransactionNo(redisSerialNumberUtils.generate(TransactionNoTypeEnum.AI_INTERVIEW.getCode()));
                        pointLog.setPoints(points);
                        pointLog.setTransactionType(TransactionTypeEnum.AI_INTERVIEW.getCode());
                        pointLog.setDurationSeconds(totalDurationSeconds);
                        pointLog.setDurationMinutes(totalMinutes);
                        pointLog.setPricingModel(pricingModel);
                        pointLog.setConsumedTokens(getConsumedTokens(callResponse));
                        pointLog.setFreezeInterviewType(pointsOperationLog.getFreezeInterviewType());
                        pointLog.setActualInterviewType(interviewType);
                        pointService.interviewDeduct(pointLog);
                    }
                }
            }
        }
    }

    /**
     * 获取消耗的token数
     * @param callResponse
     * @return
     */
    private Integer getConsumedTokens(CallResponseVO callResponse){
        LlmTokenUsageVO llmTokenUsage = callResponse.getLlmTokenUsage();
        if(llmTokenUsage!=null){
            List<Integer> values = llmTokenUsage.getValues();
            if(CollectionUtils.isNotEmpty(values)){
                int sum = values.stream()
                        .filter(Objects::nonNull) // 过滤掉 null
                        .mapToInt(Integer::intValue)
                        .sum();
                return sum;
            }
        }
        return null;
    }


    @Override
    public WrittenDetailVO getWrittenDetailResult(String callId) {
        try {
            log.info("getInterviewResult params:{}",callId);
            Map<String, String> params = new HashMap<>();
            params.put("call_id", callId);
            WrittenDetailVO writtenDetailVO = restTemplate.postForObject(aiInterviewConfig.getWrittenDetail(), params, WrittenDetailVO.class);
            if (writtenDetailVO != null) {
                writtenDetailVO.getWrittenTestAnalysis().setSkillLevel(getLevel(writtenDetailVO.getWrittenTestAnalysis().getScore()));
            }
            return writtenDetailVO;
        } catch (Exception e) {
            log.error("getInterviewResult failed",e);
        }
        return null;
    }

    private Integer getCommunicationLevel(Integer skillScore){
        if (skillScore == null){
            return null;
        } else if (skillScore >= 0 && skillScore < 2){
            return SoftSkillLevelEnum.A1.getLevel();
        } else if (skillScore >= 2 && skillScore < 4){
            return SoftSkillLevelEnum.A2.getLevel();
        } else if (skillScore >= 4 && skillScore <= 5){
            return SoftSkillLevelEnum.B1.getLevel();
        } else if (skillScore >= 6 && skillScore <= 7){
            return SoftSkillLevelEnum.B2.getLevel();
        } else if (skillScore >= 8 && skillScore <= 9){
            return SoftSkillLevelEnum.C1.getLevel();
        } else if (skillScore == 10){
            return SoftSkillLevelEnum.C2.getLevel();
        }
        return null;
    }

    private Integer getLevel(Integer skillScore){
        if (skillScore == null){
            return 0;
        } else if (skillScore >= 0 && skillScore < 40){
            return 10;
        } else if (skillScore >= 40 && skillScore < 70){
            return 20;
        } else if (skillScore >= 70 && skillScore <= 100){
            return 30;
        }
        return 0;
    }

    private Boolean interviewAnalysisCompletedSkill(AiVettedResultEntity aiVettedResultEntity,CallVO callVO){
        try {
            LocalDateTime dateTime = LocalDateTime.now();
            List<AiVettedResultSkillEntity> list = new ArrayList<>();
            if (callVO.getAnalytics().getDimensionAnalyses() != null) {
                //维度评价
                int technicalSkillScore = 0;
                for (DimensionAnalysVO dimensionAnalysVO:callVO.getAnalytics().getDimensionAnalyses()){
                    log.info("Process the content of dimension analysis :{}",dimensionAnalysVO);
                    if (CommonConstants.AI_VETTED_PERSONALITY_TEST_NAME.stream().anyMatch(d -> d.equalsIgnoreCase(dimensionAnalysVO.getDimension()))) {
                        log.info("Dimension personality test analyses :{}", dimensionAnalysVO);
                        aiVettedResultEntity.setPersonalityScore(dimensionAnalysVO.getScore());
                        continue;
                    }
                    AiVettedResultSkillEntity aiVettedResultSkillEntity = new AiVettedResultSkillEntity();
                    aiVettedResultSkillEntity.setVettedResultId(aiVettedResultEntity.getId());
                    aiVettedResultSkillEntity.setSkillName(dimensionAnalysVO.getDimension());
                    aiVettedResultSkillEntity.setSkillAssessment(dimensionAnalysVO.getFeedback());
                    aiVettedResultSkillEntity.setSkillScore(dimensionAnalysVO.getScore());
                    aiVettedResultSkillEntity.setSkillLevel(getLevel(dimensionAnalysVO.getScore()));
                    aiVettedResultSkillEntity.setCreateTime(dateTime);
                    aiVettedResultSkillEntity.setUpdateTime(dateTime);
                    list.add(aiVettedResultSkillEntity);
                    technicalSkillScore += dimensionAnalysVO.getScore();
                }
                // Calculate average skill score with proper rounding and null check
                if (!list.isEmpty()) {
                    technicalSkillScore = Math.round((float) technicalSkillScore / list.size());
                    aiVettedResultEntity.setTechnicalSkillScore(technicalSkillScore);
                }
            }
            //沟通能力
            if (callVO.getAnalytics().getCommunication() != null) {
                log.info("Process the content of communication analysis :{}",callVO.getAnalytics().getCommunication());
                CommunicationVO communication = callVO.getAnalytics().getCommunication();
                AiVettedResultSkillEntity aiVettedResultSkillEntity = new AiVettedResultSkillEntity();
                aiVettedResultSkillEntity.setVettedResultId(aiVettedResultEntity.getId());
                aiVettedResultSkillEntity.setSkillName(CommonConstants.AI_VETTED_SOFT_SKILL_NAME);
                aiVettedResultSkillEntity.setSkillAssessment(communication.getFeedback());
                aiVettedResultSkillEntity.setSkillScore(communication.getScore());
                aiVettedResultSkillEntity.setSkillLevel(getCommunicationLevel(communication.getScore()));
                aiVettedResultSkillEntity.setCreateTime(dateTime);
                aiVettedResultSkillEntity.setUpdateTime(dateTime);
                aiVettedResultEntity.setSoftSkillScore(communication.getScore() * 10);
                list.add(aiVettedResultSkillEntity);

            }
            //更新维度评价信息
            aiVettedResultSkillService.remove(new LambdaQueryWrapper<AiVettedResultSkillEntity>().eq(AiVettedResultSkillEntity::getVettedResultId,aiVettedResultEntity.getId()));
            aiVettedResultSkillService.saveBatch(list);
            aiVettedResultService.updateById(aiVettedResultEntity);
            return true;
        } catch (Exception e) {
            log.error("interviewAnalysisCompleted failed",e);
            return false;
        }
    }

    public Integer calculateTotalScore(JobEntity job, AiVettedResultEntity aiVettedResult) {
        JobEsEntity jobEsEntity = jobEsService.getJobById(job.getId());
        if (jobEsEntity == null) {
            log.warn("Failed to calculate total score: JobEsEntity not found for jobId={}", job.getId());
            return null;
        }
        if (!jobEsEntity.getIntelligenceSwitch()) {
            log.info("Intelligence switch is disabled for jobId={}, skipping score calculation", job.getId());
            return null;
        }
        // 根据入参 aiVettedResult 的 id 从数据库获取完整的 AiVettedResultEntity
        AiVettedResultEntity existResult = null;
        if (aiVettedResult != null && aiVettedResult.getId() != null) {
            existResult = aiVettedResultService.getById(aiVettedResult.getId());
        }
        if (existResult == null) {
            log.warn("Failed to calculate total score: AiVettedResultEntity not found for aiVettedResultId={}",
                    aiVettedResult != null ? aiVettedResult.getId() : "null");
            return null;
        }

        CandidateScores candidateScores = new CandidateScores();
        candidateScores.setInterviewScore(existResult.getInterviewScore());
        candidateScores.setProctoringScore(existResult.getProctoringScore());
        candidateScores.setTechnicalSkillScore(existResult.getTechnicalSkillScore());
        candidateScores.setSoftSkillScore(existResult.getSoftSkillScore());

        return intelligentJudgmentService.calculateWeightedScore(jobEsEntity, candidateScores);
    }


    @Override
    public InterviewResponseDTO getInterviewLink(GenerateInterviewLinkDTO generateInterviewLinkDTO,JobEntity jobEntity) {
        HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeadersByJob(jobEntity);
        HttpEntity<GenerateInterviewLinkDTO> requestEntity =
                new HttpEntity<>(generateInterviewLinkDTO, headers);
        InterviewResponseVO interviewResponseVO = restTemplate.postForObject(aiInterviewConfig.getGenerateInterviewLink(), requestEntity, InterviewResponseVO.class);

        return GenerateInterviewLinkConvert.INSTANCE.toAiCallbackDTO(interviewResponseVO);
    }


    @Override
    public Boolean createAIPhoneInterview(AIPhoneInterviewRequestDTO requestDTO, JobEntity jobEntity) {
        HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeadersByJob(jobEntity);
        HttpEntity<AIPhoneInterviewRequestDTO> requestEntity = new HttpEntity<>(requestDTO, headers);
        log.info("Call Ai system schedulePhoneInterview, request: {}", requestDTO);
        ResponseEntity<AIPhoneInterviewResponseDTO> result = restTemplate.postForEntity(
                aiInterviewConfig.getSchedulePhoneInterview(),
                requestEntity,
                AIPhoneInterviewResponseDTO.class
        );
        if (HttpStatus.OK == result.getStatusCode()) {
            log.info("Call Ai system schedulePhoneInterview success, response: {}", result);
            return true;
        } else {
            log.info("Call Ai system schedulePhoneInterview failed, response: {}", result);
            return false;
        }
    }

    @Override
    public Boolean recallAIPhoneInterview(String scheduleId) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("schedule_id", scheduleId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 添加调试日志
        log.info("Request headers for recallInterviewPhone: {}", headers);
        log.info("Request body for recallInterviewPhone: {}", requestBody);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        log.info("Call AI system recallInterviewPhone, scheduleId: {}", scheduleId);

        try {
            ResponseEntity<String> result = restTemplate.postForEntity(
                    aiInterviewConfig.getRecallInterviewPhone(),
                    requestEntity,
                    String.class
            );

            if (HttpStatus.OK == result.getStatusCode()) {
                log.info("Call AI system recallInterviewPhone success, scheduleId: {}, response: {}", scheduleId, result.getBody());
                return true;
            } else {
                log.warn("Call AI system recallInterviewPhone failed, scheduleId: {}, status: {}, response: {}",
                        scheduleId, result.getStatusCode(), result.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("Call AI system recallInterviewPhone exception, scheduleId: {}", scheduleId, e);
            return false;
        }
    }

    /**
     * 根据上传pdf文件生成工作描述
     * @param file
     * @return
     */
    @Override
    public JobDescriptionDTO pdfGenerateJobDescription(MultipartFile file) {
        try {
            // 校验文件类型（MIME Type）
            String contentType = file.getContentType();
            String filename = file.getOriginalFilename();
            if ((!"application/pdf".equalsIgnoreCase(contentType) && !ContentTypeEnum.DOCX.getMimeType().equalsIgnoreCase(contentType)) || filename == null || (!filename.toLowerCase().endsWith(".pdf") && !filename.toLowerCase().endsWith(".docx"))) {
                throw new BusinessException(GlobalStatusCode.UNSUPPORTED_FILE_TYPE,"Only PDF,DOCX formats are supported.");
            }
            // 1. 准备请求头
            HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            // 2. 准备请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource contentsAsResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename(); // 保持原始文件名
                }
            };
            body.add("file", contentsAsResource); // "file"应与接收方参数名一致

            // 3. 构建请求实体
            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // 4. 发送POST请求并获取响应
            JobDecResponseDTO jobDecResponseDTO = restTemplate.postForObject(
                    aiInterviewConfig.getPdfGenerateJobDescription(),
                    requestEntity,
                    JobDecResponseDTO.class
            );
            if (Objects.nonNull(jobDecResponseDTO) && jobDecResponseDTO.getCode() == 0) {
                JobDescriptionDTO jobDescriptionDTO = jobDecResponseDTO.getData();
                if (Objects.isNull(jobDescriptionDTO) || Objects.isNull(jobDescriptionDTO.getWorkplace()) || jobDescriptionDTO.getWorkplace().isEmpty()) {
                    return jobDescriptionDTO;
                }
                List<LocationAIDTO> locations = jobDescriptionDTO.getWorkplace();
                Iterator<LocationAIDTO> it = locations.iterator();
                while (it.hasNext()) {
                    LocationAIDTO location = it.next();
                    if (!StringUtils.hasText(location.getCityName()) && !StringUtils.hasText(location.getCityName()) && !StringUtils.hasText(location.getCityName())) {
                        it.remove();
                        continue;
                    }
                    if (StringUtils.hasText(location.getCityName())) {
                        LambdaQueryWrapper<CityEntity> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(CityEntity::getName, location.getCityName());
                        queryWrapper.last("LIMIT 1");
                        CityEntity cityEntity = cityMapper.selectOne(queryWrapper);
                        if (Objects.nonNull(cityEntity)) {
                            location.setCityId( cityEntity.getId());
                            if (StringUtils.hasText(location.getStateName())) {
                                location.setStateId( cityEntity.getStateId());
                            } else {
                                StateEntity stateEntity = stateMapper.selectById(cityEntity.getStateId());
                                location.setStateId( stateEntity.getId());
                                location.setStateName(stateEntity.getName());
                            }
                            if (StringUtils.hasText(location.getCountryName())) {
                                location.setCountryId(cityEntity.getCountryId());
                            } else {
                                CountryEntity countryEntity = countryMapper.selectById(cityEntity.getCountryId());
                                location.setCountryId( countryEntity.getId());
                                location.setCountryName(countryEntity.getName());
                            }
                        }
                    }
                    if (StringUtils.hasText(location.getStateName()) && location.getStateId() == null) {
                        LambdaQueryWrapper<StateEntity> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(StateEntity::getName, location.getStateName());
                        queryWrapper.last("LIMIT 1");
                        StateEntity stateEntity = stateMapper.selectOne(queryWrapper);
                        if (Objects.nonNull(stateEntity)) {
                            location.setStateId( stateEntity.getId());
                            if (StringUtils.hasText(location.getCountryName())) {
                                location.setCountryId(stateEntity.getCountryId());
                            } else {
                                CountryEntity countryEntity = countryMapper.selectById(stateEntity.getCountryId());
                                location.setCountryId( countryEntity.getId());
                                location.setCountryName(countryEntity.getName());
                            }
                        }
                    }
                    if (StringUtils.hasText(location.getCountryName()) && location.getCountryId() == null) {
                        LambdaQueryWrapper<CountryEntity> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(CountryEntity::getName, location.getCountryName());
                        queryWrapper.last("LIMIT 1");
                        CountryEntity countryEntity = countryMapper.selectOne(queryWrapper);
                        location.setCountryId(countryEntity != null ? countryEntity.getId() : null);
                    }
                    List<String> joinstr = Arrays.asList(location.getCityName(),location.getStateName(),location.getCountryName());
                    String locationName = joinstr.stream().filter(Objects::nonNull).collect(Collectors.joining(","));
                    location.setLocationName(locationName);
                    if (Objects.isNull(location.getGeoPoint())) {
                        GeoPointDTO geoPointDTO = new GeoPointDTO();
                        geoPointDTO.setLatitude(0d);
                        geoPointDTO.setLongitude(0d);
                        location.setGeoPoint(geoPointDTO);
                    }
                }

                if (StringUtils.hasText(jobDescriptionDTO.getJobType())) {
                    List<JobTypeDto> types = jobTypeService.getAllJobTypes();
                    Map<String, Integer> typeMap = types.stream().collect(Collectors.toMap(JobTypeDto::getName, JobTypeDto::getId));
                    jobDescriptionDTO.setTypeId(typeMap.get(jobDescriptionDTO.getJobType()));
                }
                if (StringUtils.hasText(jobDescriptionDTO.getWorkLocationType())){
                    List<JobModeDto> modes = jobModeService.getAll();
                    Map<String, Integer> modeMap = modes.stream().collect(Collectors.toMap(JobModeDto::getName, JobModeDto::getId));
                    jobDescriptionDTO.setModeId(modeMap.get(jobDescriptionDTO.getWorkLocationType()));
                }
                if (StringUtils.hasText(jobDescriptionDTO.getDomain())) {
                    List<JobCategoryDto> categories = jobCategoryService.getAllJobCategories();
                    Map<String, Integer> categorieMap = categories.stream().collect(Collectors.toMap(JobCategoryDto::getName, JobCategoryDto::getId));
                    jobDescriptionDTO.setCategoryId(categorieMap.get(jobDescriptionDTO.getDomain()));
                }
                if (StringUtils.hasText(jobDescriptionDTO.getCurrencyType()) || StringUtils.hasText(jobDescriptionDTO.getCurrencyType())) {
                    List<DictionaryEntity> dictionaryEntities = dictionaryService.list();
                    Map<String, Map<String, DictionaryEntity>> dictionaryMap = dictionaryEntities.stream()
                            // 按type分组
                            .collect(Collectors.groupingBy(
                                    DictionaryEntity::getType,
                                    // 每组内再按value分组，保留对应的实体
                                    Collectors.toMap(
                                            entity -> entity.getCode().toLowerCase(),
                                            entity -> entity,
                                            // 若存在相同value的实体，保留第一个
                                            (existing, replacement) -> existing
                                    )
                            ));
                    // 优化字典查询逻辑
                    Optional.ofNullable(jobDescriptionDTO.getCurrencyType())
                            .map(String::toLowerCase)
                            .flatMap(lowercasecurrencyType ->
                                    Optional.ofNullable(dictionaryMap.get(DictionaryEnum.REPORT.getName()))
                                            .filter(map -> !map.isEmpty())
                                            .map(map -> map.get(lowercasecurrencyType))
                            )
                            .filter(Objects::nonNull)
                            .map(DictionaryEntity::getId)
                            .ifPresent(jobDescriptionDTO::setCurrency);
                    Optional.ofNullable(jobDescriptionDTO.getSalaryType())
                            .map(String::toLowerCase)
                            .flatMap(lowercaseSalaryType ->
                                    Optional.ofNullable(dictionaryMap.get(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()))
                                            .filter(map -> !map.isEmpty())
                                            .map(map -> map.get(lowercaseSalaryType))
                            )
                            .filter(Objects::nonNull)
                            .map(DictionaryEntity::getId)
                            .ifPresent(jobDescriptionDTO::setSalaryTypeId);
                }
                return jobDescriptionDTO;
            }
            return null;
        } catch (Exception e) {
            log.info("pdfGenerateJobDescription failed:",e);
            throw new BusinessException(CommonResponseCode.PDF_GENERATION_FAILED);
        }
    }

    @Override
    public String getCallId(Long candidateJobId) {
        try {
            LambdaQueryWrapper<AiCallbackEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AiCallbackEntity::getCandidateJobId, candidateJobId);
            queryWrapper.last("LIMIT 1");
            AiCallbackEntity aiCallbackEntity = aiCallbackMapper.selectOne(queryWrapper);
            return Objects.nonNull(aiCallbackEntity)?aiCallbackEntity.getCallId():null;
        } catch (Exception e) {
            log.error("getCallId failed:",e);
            return null;
        }
    }

    /**
     * 获取面试报告S3 key
     * 返回S3文件的key
     *
     * @return
     */
    public String exportInterviewReport(String callId) {
        try {
            // 构建下载URL
            String downloadUrl = aiInterviewConfig.getExportInterviewReport() + "?return_html=false&call_id=" + callId;
            log.info("Starting to get interview report S3 key for callId: {}, url: {}", callId, downloadUrl);

            // 使用RestTemplate获取VO响应
            ResponseEntity<InterviewReportResponseVO> responseEntity = restTemplate.getForEntity(downloadUrl, InterviewReportResponseVO.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                InterviewReportResponseVO responseVO = responseEntity.getBody();
                String interviewReportS3Key = responseVO.getInterviewReportS3Key();
                log.info("Successfully got interview report S3 key for callId: {}, s3Key: {}", callId, interviewReportS3Key);
                return interviewReportS3Key;
            } else {
                log.error("Failed to get interview report S3 key for callId: {}, status: {}",
                        callId, responseEntity.getStatusCode());
                throw new BusinessException(GlobalStatusCode.FAIL, "Interview report S3 key not found");
            }

        } catch (Exception e) {
            log.error("exportInterviewReport failed for callId: {}", callId, e);
            throw new BusinessException(GlobalStatusCode.FAIL, "Failed to get interview report S3 key: " + e.getMessage());
        }
    }

    @Override
    public CandidateRequestVO resumeParsing(MultipartFile multipartFile) {
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return s3Utils.uploadFile(multipartFile);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    log.error("resume upload s3 failed:",e);
                    throw new BusinessException(GlobalStatusCode.FAIL,"resume upload s3 failed");
                }
            }, aiTaskExecutor);
            // 设置请求头为multipart/form-data
            HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 构建请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 直接包装MultipartFile的字节流
            ByteArrayResource fileResource = new ByteArrayResource(multipartFile.getBytes()) {
                @Override
                public String getFilename() {
                    // 必须重写getFilename()，否则服务端可能无法获取文件名
                    return multipartFile.getOriginalFilename();
                }
            };

            // 添加文件参数（与服务端接口参数名保持一致）
            body.add("file", fileResource);

            // 构建请求实体
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送POST请求
            ResponseEntity<AIResultVO<ResumeParsingRsultVO>> response = restTemplate.exchange(
                    aiInterviewConfig.getResumeParsingUrl(), HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<AIResultVO<ResumeParsingRsultVO>>() {}
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                if (Objects.isNull(response.getBody())) {
                    return null;
                }
                ResumeParsingRsultVO resumeParsingRsultVO = response.getBody().getData();
                List<DictionaryEntity> dictionaryEntities = dictionaryService.list();
                Map<String, Map<String, DictionaryEntity>> dictionaryMap = dictionaryEntities.stream()
                        // 按type分组
                        .collect(Collectors.groupingBy(
                                DictionaryEntity::getType,
                                // 每组内再按value分组，保留对应的实体
                                Collectors.toMap(
                                        DictionaryEntity::getValue,
                                        entity -> entity,
                                        // 若存在相同value的实体，保留第一个
                                        (existing, replacement) -> existing
                                )
                        ));
                CandidateRequestVO candidateRequestVO = CandidateConverter.INSTANCE.convertToCandidateRequestVO(resumeParsingRsultVO);

                if (resumeParsingRsultVO != null && StringUtils.hasText(resumeParsingRsultVO.getCityName())) {
                    LambdaQueryWrapper<CityEntity> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(CityEntity::getName, resumeParsingRsultVO.getCityName());
                    queryWrapper.last("LIMIT 1");
                    CityEntity cityEntity = cityMapper.selectOne(queryWrapper);
                    if (Objects.nonNull(cityEntity)) {
                        candidateRequestVO.setCityId( cityEntity.getId());
                        if (candidateRequestVO.getStateId() == null) {
                            candidateRequestVO.setStateId( cityEntity.getStateId());
                        }
                        if (candidateRequestVO.getCountryId() == null) {
                            candidateRequestVO.setCountryId( cityEntity.getCountryId());
                        }
                    } else {
                        candidateRequestVO.setCityName(null);
                    }
                }
                if (resumeParsingRsultVO != null && StringUtils.hasText(resumeParsingRsultVO.getStateName()) && candidateRequestVO.getStateId() == null) {
                    LambdaQueryWrapper<StateEntity> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(StateEntity::getName, resumeParsingRsultVO.getStateName());
                    queryWrapper.last("LIMIT 1");
                    StateEntity stateEntity = stateMapper.selectOne(queryWrapper);
                    if (Objects.nonNull(stateEntity)) {
                        candidateRequestVO.setStateId( stateEntity.getId());
                        if (candidateRequestVO.getCountryId() == null) {
                            candidateRequestVO.setCountryId(stateEntity.getCountryId());
                        }
                    } else {
                        candidateRequestVO.setStateName(null);
                    }
                }
                if (resumeParsingRsultVO != null && StringUtils.hasText(resumeParsingRsultVO.getCountryName()) && candidateRequestVO.getCountryId() == null) {
                    LambdaQueryWrapper<CountryEntity> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(CountryEntity::getName, resumeParsingRsultVO.getCountryName());
                    queryWrapper.last("LIMIT 1");
                    CountryEntity countryEntity = countryMapper.selectOne(queryWrapper);
                    if (Objects.nonNull(countryEntity)) {
                        candidateRequestVO.setCountryId(countryEntity != null ? countryEntity.getId() : null);
                    } else {
                        candidateRequestVO.setCountryName(null);
                    }
                }

                if (resumeParsingRsultVO != null) {
                    // 优化字典查询逻辑
                    Optional.ofNullable(dictionaryMap.get(DictionaryEnum.REPORT.getName()))
                        .filter(map -> !map.isEmpty())
                        .map(map -> map.get(resumeParsingRsultVO.getCurrencyType()))
                        .filter(Objects::nonNull)
                        .map(DictionaryEntity::getId)
                        .ifPresent(candidateRequestVO::setCurrencyTypeId);

                    Optional.ofNullable(dictionaryMap.get(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()))
                        .filter(map -> !map.isEmpty())
                        .map(map -> map.get(resumeParsingRsultVO.getSalaryType()))
                        .filter(Objects::nonNull)
                        .map(DictionaryEntity::getId)
                        .ifPresent(candidateRequestVO::setCurrencyTypeId);

                    List<ResumeCandidateEducationVO> educationResultList = resumeParsingRsultVO.getEducationList();
                    List<CandidateEducationVO> educationList = new ArrayList<>();
                    candidateRequestVO.setEducationList(educationList);
                    if (Objects.nonNull(educationResultList)) {
                        educationResultList.forEach(education -> {
                            CandidateEducationVO candidateEducationVO = CandidateConverter.INSTANCE.convertToCandidateEducationVO(education);

                            // 优化教育信息设置
                            Optional.ofNullable(dictionaryMap.get(DictionaryEnum.CAMER_RECORDING_URL.getName()))
                                .filter(map -> !map.isEmpty())
                                .map(map -> map.get(education.getInstitutionType()))
                                .filter(Objects::nonNull)
                                .map(DictionaryEntity::getId)
                                .ifPresent(candidateEducationVO::setInstitutionTypeId);

                            Optional.ofNullable(dictionaryMap.get(DictionaryEnum.DEGREE_TYPE.getName()))
                                .filter(map -> !map.isEmpty())
                                .map(map -> map.get(education.getDegree()))
                                .filter(Objects::nonNull)
                                .map(DictionaryEntity::getId)
                                .ifPresent(candidateEducationVO::setDegreeId);

                            educationList.add(candidateEducationVO);
                        });

                        // 设置薪资类型ID
                        Optional.ofNullable(dictionaryMap.get(DictionaryEnum.REPORT.getName()))
                            .filter(map -> !map.isEmpty())
                            .map(map -> map.get(resumeParsingRsultVO.getSalaryType()))
                            .filter(Objects::nonNull)
                            .map(DictionaryEntity::getId)
                            .ifPresent(candidateRequestVO::setCurrencyTypeId);
                    }
                }
                String pdfUrl = future.get(aiInterviewConfig.getPdfTimeOut(), TimeUnit.MINUTES);
                candidateRequestVO.setResumeUrl(pdfUrl);
                Integer genderCode = GenderEnum.getCodeByValue(candidateRequestVO.getGender());
                if (genderCode != null) {
                    candidateRequestVO.setGender(genderCode.toString());
                }
                return candidateRequestVO;
            } else {
                throw new BusinessException(GlobalStatusCode.FAIL,"ai resumeParsing failed");
            }
        } catch (Exception e) {
            log.error("resumeParsing failed:",e);
            throw new BusinessException(GlobalStatusCode.FAIL,"ai resumeParsing failed");
        }
    }

    @Override
    public UpdateInterviewTypeResponseVO updateInterviewType(UpdateInterviewTypeDTO updateInterviewTypeDTO) {
        try {
            log.info("Starting to update interview type, params: {}", updateInterviewTypeDTO);
            
            // Validate interview type
            InterviewTypeEnum interviewType = InterviewTypeEnum.getByName(updateInterviewTypeDTO.getInterviewType());
            if (interviewType == null) {
                log.error("Invalid interview type: {}", updateInterviewTypeDTO.getInterviewType());
                throw new BusinessException(GlobalStatusCode.FAIL, "Invalid interview type: " + updateInterviewTypeDTO.getInterviewType());
            }
            
            // Call AI interview service to update interview type
            ResponseEntity<UpdateInterviewTypeResponseVO> response = restTemplate.exchange(
                    aiInterviewConfig.getUpdateInterviewTypeUrl(),
                    HttpMethod.PUT,
                    new HttpEntity<>(updateInterviewTypeDTO),
                    UpdateInterviewTypeResponseVO.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UpdateInterviewTypeResponseVO result = response.getBody();
                log.info("Interview type updated successfully, interviewId: {}, interviewName: {}, newType: {}", 
                        result.getId(), result.getName(), result.getInterviewType());
                return result;
            } else {
                log.error("Failed to update interview type, status code: {}", response.getStatusCode());
                throw new BusinessException(GlobalStatusCode.FAIL, "Failed to update interview type");
            }
            
        } catch (Exception e) {
            log.error("Exception occurred while updating interview type, params: {}", updateInterviewTypeDTO, e);
            throw new BusinessException(GlobalStatusCode.FAIL, "Failed to update interview type: " + e.getMessage());
        }
    }

    /**
     * 获取实际面试类型
     * @param callId
     * @return
     */
    @Override
    public Integer getInterviewType(String callId) {
        try{
            Map<String, String> params = new HashMap<>();
            params.put("call_id", callId);
            InterviewTypeResultVO interviewTypeResultVO = restTemplate.postForObject(aiInterviewConfig.getGetInterviewType(), params, InterviewTypeResultVO.class);
            if (interviewTypeResultVO!=null){
                InterviewTypeDataResultVO data = interviewTypeResultVO.getData();
                if (data!=null){
                    String interviewType = data.getInterviewType();
                    log.info("getInterviewType callId:{},interviewType:{}",callId,interviewType);
                    if (StringUtils.hasText(interviewType) && (InterviewTypeEnum.VIDEO.getName().equals(interviewType) || InterviewTypeEnum.AUDIO.getName().equals(interviewType))){
                        return Integer.valueOf(InterviewTypeEnum.getByName(interviewType).getCode());

                    }
                }
            }
            return null;
        }catch (Exception e){
            log.error("Exception occurred while retrieving interview type, params: {}", callId, e);
        }
        return null;
    }

    private AiCallbackEntity buildAiCallback(Long candidateJobId, AiCallbackDTO dto, LocalDateTime now) {
        AiCallbackEntity aiCallback = AiCallbackConvert.INSTANCE.toAiCallbackEntity(dto);
        AiCallbackDataVO data = dto.getData();
        aiCallback.setCandidateJobId(candidateJobId);
        if (data.getTimestamp() == null) {
            aiCallback.setTimestamp(now);
        } else {
            aiCallback.setTimestamp(data.getTimestamp());
        }
        aiCallback.setInterviewId(data.getInterviewId());
        aiCallback.setInterviewerPhone(data.getCandidatePhone());
        aiCallback.setInterviewerEmail(data.getEmail());
        aiCallback.setCallId(data.getCallId());
        aiCallback.setCreateTime(now);
        aiCallback.setUpdateTime(now);
        aiCallback.setStatus(AICallbackStatusEnum.PR_D.getCode());
        return aiCallback;
    }

    private CandidateJobEntity fillCandidateJob(CandidateJobEntity candidateJob, AiCallbackDataVO data) {
        AnalyticsVO analytics = data.getAnalytics();
        CallDetailsVO details = data.getDetails();
        if (analytics == null) {
            analytics = new AnalyticsVO();
        }

        candidateJob.setOverallScore(analytics.getOverallScore());
        candidateJob.setPhoneRecordingUrl(details.getRecordingUrl());
        if (details.getStartTimestamp() != null) {
            candidateJob.setInterviewStartTime(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(details.getStartTimestamp()), ZoneId.systemDefault())
            );
            candidateJob.setInterviewTime(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(details.getStartTimestamp()), ZoneId.systemDefault())
            );
        }
        if (details.getEndTimestamp() != null) {
            candidateJob.setInterviewEndTime(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(details.getEndTimestamp()), ZoneId.systemDefault())
            );
        }
        //Ai电话面试没有作弊分
        //candidateJob.setCheatingAnalysis();
        candidateJob.setInterviewPhoneStatus(
                InterviewPhoneStatusEnum.change(candidateJob.getInterviewPhoneStatus(), true).getCode()
        );
        return candidateJob;
    }

    private AiVettedResultEntity buildAiVettedResult(Long candidateJobId, AiCallbackDataVO data, LocalDateTime now) {
        AnalyticsVO analytics = data.getAnalytics();
        CallDetailsVO details = data.getDetails();
        if (analytics == null) {
            analytics = new AnalyticsVO();
        }
        if (details == null) {
            details = new CallDetailsVO();
        }

        AiVettedResultEntity aiVettedResult = new AiVettedResultEntity();
        aiVettedResult.setId(candidateJobId);
        aiVettedResult.setCandidateJobId(candidateJobId);
        aiVettedResult.setInterviewType(InterviewTypeEnum.AI_PHONE.getCode());
        aiVettedResult.setInterviewTime(
                LocalDateTime.ofInstant(Instant.ofEpochMilli(details.getStartTimestamp()), ZoneId.systemDefault())
        );
        aiVettedResult.setInterviewScore(analytics.getOverallScore());
        //Ai电话面试没有作弊分
        //aiVettedResult.setProctoringScore();
        aiVettedResult.setOverallSkillAssessment(analytics.getOverallFeedback());
        aiVettedResult.setTranscript( transcriptObjectConverter.convertToJson(details.getTranscriptObject()) );
        aiVettedResult.setDataSource(DataSourceEnum.RECRUIT.getSource());
        aiVettedResult.setPhoneRecordingUrl(details.getRecordingUrl());
        aiVettedResult.setCreateTime(now);
        aiVettedResult.setUpdateTime(now);
        return aiVettedResult;
    }

    private List<AiVettedResultSkillEntity> buildAiVettedSkills(AnalyticsVO analytics, LocalDateTime now) {
        List<AiVettedResultSkillEntity> skills = new ArrayList<>();
        if (analytics == null) {
            return skills;
        }
        if (analytics.getCommunication() != null) {
            CommunicationVO communication = analytics.getCommunication();
            AiVettedResultSkillEntity aiVettedResultSkillEntity = new AiVettedResultSkillEntity();
            aiVettedResultSkillEntity.setSkillName(CommonConstants.AI_VETTED_SOFT_SKILL_NAME);
            aiVettedResultSkillEntity.setSkillAssessment(communication.getFeedback());
            aiVettedResultSkillEntity.setSkillScore(communication.getScore());
            aiVettedResultSkillEntity.setSkillLevel(getCommunicationLevel(communication.getScore()));
            aiVettedResultSkillEntity.setCreateTime(now);
            aiVettedResultSkillEntity.setUpdateTime(now);
            skills.add(aiVettedResultSkillEntity);
        }
        if (CollectionUtils.isEmpty(analytics.getDimensionAnalyses())) {
            log.info("AI interview analytics dimension is empty.");
            return skills;
        }
        for (DimensionAnalysVO dimensionAnalysVO : analytics.getDimensionAnalyses()) {
            AiVettedResultSkillEntity aiVettedResultSkillEntity = new AiVettedResultSkillEntity();
            aiVettedResultSkillEntity.setSkillName(dimensionAnalysVO.getDimension());
            aiVettedResultSkillEntity.setSkillAssessment(dimensionAnalysVO.getFeedback());
            aiVettedResultSkillEntity.setSkillScore(dimensionAnalysVO.getScore());
            aiVettedResultSkillEntity.setSkillLevel(getLevel(dimensionAnalysVO.getScore()));
            aiVettedResultSkillEntity.setCreateTime(now);
            aiVettedResultSkillEntity.setUpdateTime(now);
            skills.add(aiVettedResultSkillEntity);
        }
        return skills;
    }

    private CallVO buildCallVO(AiCallbackDataVO aiCallbackData) {
        CallVO callVO = new CallVO();
        if (aiCallbackData.getDetails() == null) {
            return callVO;
        }
        if (aiCallbackData.getDetails().getCallCost() == null) {
            return callVO;
        }
        CallCostVO callCost = new CallCostVO();
        callCost.setTotalDurationSeconds(aiCallbackData.getDetails().getCallCost().getTotalDurationSeconds());
        CallResponseVO callResponse = new CallResponseVO();
        callResponse.setCallCost(callCost);

        callVO.setCallResponse(callResponse);
        return callVO;
    }



}
