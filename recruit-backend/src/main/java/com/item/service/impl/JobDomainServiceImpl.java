package com.item.service.impl;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.item.convert.JobConvert;
import com.item.dto.CityDTO;
import com.item.dto.CompanyInfoSimpleDTO;
import com.item.dto.CountryDTO;
import com.item.dto.CurrencyTypeDTO;
import com.item.dto.DictionaryDTO;
import com.item.dto.JobDto;
import com.item.dto.JobModeDto;
import com.item.dto.JobOptionDTO;
import com.item.dto.SalaryTypeDTO;
import com.item.dto.StateDTO;
import com.item.dto.ai.InterviewDataDTO;
import com.item.dto.ai.InterviewRequestDTO;
import com.item.dto.ai.InterviewUpdateRequestDTO;
import com.item.dto.ai.QuestionGenerationDTO;
import com.item.dto.ai.UpdateInterviewTypeDTO;
import com.item.dto.ayrshare.AyrSharePostDTO;
import com.item.dto.iam.IamCompanyDTO;
import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.job.*;
import com.item.dto.job.GeoPointDTO;
import com.item.dto.job.IntelligenceScoreRuleDTO;
import com.item.dto.job.JobCreateBO;
import com.item.dto.job.JobCreateDTO;
import com.item.dto.job.JobDetailDTO;
import com.item.dto.job.JobHistoryListRequestDTO;
import com.item.dto.job.JobUpdateBO;
import com.item.dto.job.JobUpdateDTO;
import com.item.dto.job.LocationDTO;
import com.item.dto.job.LocationValDTO;
import com.item.dto.job.LocationValRecordDTO;
import com.item.dto.job.RandomJobRecommendRequestDTO;
import com.item.dto.job.*;
import com.item.entity.JobCategoryEntity;
import com.item.entity.JobEntity;
import com.item.entity.JobModeEntity;
import com.item.entity.JobTypeEntity;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.AiInterviewConfig;
import com.item.framework.config.BusinessDeductionPointsConfig;
import com.item.framework.config.properties.JobIntelligenceScoreRuleConfigProperties;
import com.item.framework.constant.*;

import static com.item.framework.constant.CommonResponseCode.COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER;
import static com.item.framework.constant.JobResponseCode.JOB_LOCATION_TYPE_NOT_FOUND;
import static com.item.framework.constant.JobResponseCode.JOB_SALARY_TYPE_NOT_FOUND;

import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.service.*;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.util.CommonUtils;
import com.item.util.HashUtils;
import com.item.util.LambdaUtil;
import com.item.util.RedisKeyUtil;
import com.item.util.RedisSerialNumberUtils;
import com.item.util.S3Utils;
import com.item.util.UserContextUtil;
import com.item.vo.JobDetailVO;
import com.item.vo.JobHistoryDetailVO;
import com.item.vo.JobListVO;
import com.item.vo.JobOptionVO;
import com.item.vo.LocationTypeVO;
import com.item.vo.ai.InterviewResultVO;
import com.item.vo.ai.InterviewUpdateResultVO;
import com.item.vo.ai.UpdateInterviewTypeResponseVO;
import com.item.vo.job.JobCreateResponseVO;
import com.item.vo.job.JobUpdateResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.item.service.client.IamUserClient;
/**
 * @author hua.liu
 */
@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class JobDomainServiceImpl implements JobDomainService {
    @Value("${company.name.replace.str:,.，。#@%$}")
    private String companyNameReplace;
    @Value("${job.publish.url:https://recruit-dev.item.pub}")
    private String jobPublishUrl;
    @Value("${job.publish.route.info.prefix:job-details}")
    private String jobPublishRouteInfoPrefix;
    @Value("${job.publish.route.list.prefix:job-list}")
    private String jobPublishRouteListPrefix;
    @Value("${job.publish.enable.ayr.share:false}")
    private boolean jobPublishAyrShare;
    @Value("${job.publish.list.type:}")
    private String jobPublishListType;
    @Value("${job.publish.info.type:}")
    private String jobPublishInfoType;
    @Value("${job.publish.share.case:{0} is hiring for {1}. Click {2} to apply for the job.}")
    private String jobPublishShareCase;
    @Value("${job.publish.share.title.case:{0} is hiring for {1}.}")
    private String jobPublishShareTitleCase;


    private final JobModeService jobModeService;
    private final JobCategoryService jobCategoryService;
    private final JobTypeService jobTypeService;
    private final JobService jobService;
    private final JobConvert jobConvert;
    private final JobDoubleDataSourceService jobDoubleDataSourceService;
    private final RedissonClient redissonClient;
    private final JobEsService jobEsService;
    private final ShortIdGenerator shortIdGenerator;
    private final AyrShareService ayrShareService;
    private final NaukriService naukriService;
    private final AIServiceImpl aiService;
    private final LocationService locationService;
    private final AiInterviewConfig aiInterviewConfig;
    private final IamRpcAdapter iamRpcAdapter;
    private final S3Utils s3Utils;
    private final CandidateJobService candidateJobService;
    private final DictionaryService dictionaryService;
    private final ThreadPoolTaskExecutor aiTaskExecutor;
    private final PointService pointService;
    private final BusinessDeductionPointsConfig businessDeductionPointsConfig;
    private final RedisSerialNumberUtils redisSerialNumberUtils;
    private final GenerateUrlCodeService generateUrlCodeService;

    private final LoadingCache<String, CompanyInfoSimpleDTO> companyInfoLoadingCache;
    private final XmlFeedService xmlFeedService;
    private final XmlFeedConfigService xmlFeedConfigService;
    private final AyrshareCompanyConfigService ayrshareCompanyConfigService;
    private final JobIntelligenceScoreRuleConfigProperties jobIntelligenceScoreRuleConfigProperties;
    private final PlaceService placeService;
    private final ThreadPoolTaskExecutor locationTaskExecutor;
    private final IamUserClient iamUserClient;

    private final JobApprovalService jobApprovalService;
    private final JobAuditHistoryService jobAuditHistoryService;

    @Override
    public JobCreateResponseVO publishJob(JobCreateDTO dto) {
        log.info("job create dto {}", dto);
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        dto.checkJobRequirement();
        dto.validateCheckpointConfig();
        dto.trim();
        checkInterviewLength(dto.getInterviewType(), dto.getInterviewLength());
        checkWrittenTestForAiPhone(dto.getInterviewType(), dto.getEnableWrittenTest());
        checkLocations(dto.getLocations());
        // 验证智能评估配置
        validateIntelligenceConfig(dto.getInterviewType(), dto.getIntelligenceSwitch(), dto.getScoreRules());
        //自动分享校验
        if (HotListType.isEnabled(dto.getHotList())) {
            ayrshareCompanyConfigService.checkAllowHotList(currentUserNeedLogin.getCompanyCode());
        }
        JobCreateBO jobCreateBO = jobConvert.toJobCreateBO(dto);
        IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(currentUserNeedLogin.getCompanyCode());
        jobCreateBO.fillLocationName();
        jobCreateBO.setCustomerName(companyInfo.getCompanyName());
        jobCreateBO.setCreateUser(currentUserNeedLogin.getUserName());
        jobCreateBO.setCompanyCode(currentUserNeedLogin.getCompanyCode());
        jobCreateBO.setCreateBy(Long.parseLong(currentUserNeedLogin.getId()));
        jobCreateBO.setUpdateBy(Long.parseLong(currentUserNeedLogin.getId()));
        // 更新的人
        jobCreateBO.setUpdateUser(currentUserNeedLogin.getUserName());
        jobCreateBO.setModeName(Optional.ofNullable(getMode(jobCreateBO.getModeId())).map(JobModeEntity::getModeName).orElse(""));
        jobCreateBO.setTypeName(Optional.ofNullable(jobTypeService.getById(jobCreateBO.getTypeId())).map(JobTypeEntity::getName).orElse(""));
        jobCreateBO.setCategoryName(Optional.ofNullable(jobCategoryService.getById(jobCreateBO.getCategoryId())).map(JobCategoryEntity::getName).orElse(""));

        String companyName = CommonUtils.cleanInputReplace(jobCreateBO.getCustomerName(), companyNameReplace);
        String title = CommonUtils.cleanInputReplace(jobCreateBO.getTitle(), companyNameReplace);
        String urlCode = CommonUtils.join(companyName, title);
        jobCreateBO.setUrlCode(urlCode);
        jobCreateBO.setLogoPath(companyInfo.getLogopath());
        jobCreateBO.setCurrencyName(getCurrencyName(dto.getCurrency()));
        jobCreateBO.setSalaryTypeName(getSalaryTypeName(dto.getSalaryType()));
        // Generate hash for company name and title for uniqueness validation
        String companyTitleHash = HashUtils.murmur3Hash(urlCode);
        jobCreateBO.setCompanyTitleHash(companyTitleHash);
        RLock lock = redissonClient.getLock(RedisKeyUtil.getPublishJobKey(jobCreateBO.getCompanyCode()));
//       //是否进行了积分冻结
//        boolean isFreeze=false;
//        PointLogVO pointLogVO=new PointLogVO();
//        //交易号、扣减积分
//        pointLogVO.setTransactionNo(redisSerialNumberUtils.generate(TransactionNoTypeEnum.PUBLISH_JOB.getCode()));
//        pointLogVO.setPoints(businessDeductionPointsConfig.getPublishJob().getDeductedPoints());
//        pointLogVO.setUserId(Long.parseLong(currentUserNeedLogin.getId()));
//        pointLogVO.setTransactionType(TransactionTypeEnum.PUBLISH_JOB.getCode());
//        pointLogVO.setRemark(jobCreateBO.getTitle());
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {

//                //校验、冻结职位发布积分
//                isFreeze=pointService.checkPointsFreeze(pointLogVO);
//                if (!isFreeze){
//                    throw new BusinessException(UnChangeResponseCode.INSUFFICIENT_POINTS);
//                }
                //校验岗位是否重复 重复直接返回 二次确认
                if (checkJobDuplication(jobCreateBO.getTitle(), jobCreateBO.getLocations(), jobCreateBO.getCompanyCode(), null, jobCreateBO.isConfirmSave())) {
                    return new JobCreateResponseVO(null, true);
                }
                // 这里直接发布了 后续接入central扣减积分了 在看具体状态
                jobCreateBO.setAyrshareStatus(AyrshareStatus.NO_SHARE.getCode());
                jobCreateBO.setJobStatus(JobStatus.PENDING_REVIEW.getCode());
                jobCreateBO.setSubmittedForApprovalAt(LocalDateTime.now());

                Long jobId = jobDoubleDataSourceService.publishJob(jobCreateBO);

                var adminEmail = iamUserClient.getAdminEmailByCompanyCode(jobCreateBO.getCompanyCode());

                jobApprovalService.sendApprovalNotification(jobId, JobApprovalAction.SUBMITTED.toString(), null, adminEmail);

                // 是否自动发布到其他网站 对接
                ayrSharePost(jobCreateBO, companyInfo, jobId);

                // Naukri posting for India or Saudi Arabia
               try {
                   if (naukriService.shouldPostToNaukri(jobCreateBO)) {
                       naukriService.asyncPostJob(jobCreateBO, companyName, jobId);
                   } else {
                       log.info("Naukri post skipped by country or toggle for jobId={} locations={}", jobId, jobCreateBO.getLocations());
                   }
               } catch (Exception ex) {
                   log.warn("Naukri post invoke failed jobId={} error=", jobId, ex);
               }

//                //调用扣除积分接口
//                pointLogVO.setJobId(jobId);
//                pointService.confirmPointsFreeze(pointLogVO);

                //TODO 增加面试时长 发送到ai服务
                //生成url id 异步处理，定时任务补偿
                
                // 异步创建 Place 信息
                if (CollectionUtils.isNotEmpty(jobCreateBO.getLocations())) {
                    jobCreateBO.getLocations().stream()
                        .map(LocationValDTO::getPlaceId)
                        .filter(StringUtils::isNotBlank)
                        .distinct()  // 去重，避免重复处理相同的 placeId
                        .forEach(placeId -> {
                            locationTaskExecutor.execute(() -> {
                                try {
                                    placeService.asyncCreatePlaceInfo(placeId);
                                } catch (Exception e) {
                                    log.error("Failed to create place info for placeId: {}", placeId, e);
                                }
                            });
                        });
                }
                
                aiTaskExecutor.execute(() -> {
                    long start = System.currentTimeMillis();
                    log.info("createAIInterview jobId:{}", jobId);
                    InterviewResultVO interviewResultVO = this.createAIInterview(jobCreateBO);
                    log.info("createAIInterview jobId:{},time:{},结果:{}", jobId, System.currentTimeMillis() - start, interviewResultVO);
                    jobCreateBO.setInterviewUrlId(interviewResultVO.getUrlId());
                    jobService.updateInterviewUrlId(jobId, jobCreateBO.getInterviewUrlId());
                });
                //异步生成 feed xml
                aiTaskExecutor.execute(() -> {
                    xmlFeedConfigService.jobUpdate(currentUserNeedLogin.getCompanyCode(),Long.valueOf(currentUserNeedLogin.getId()));
                });
                return new JobCreateResponseVO(jobId, false);
            } else {
                throw BusinessException.of(COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER);
            }
        } catch (BusinessException bex) {
            log.warn("publish job error dto {} bo {}", dto, jobCreateBO, bex);
//                //如果进行了积分冻结，则进行解冻
//                if(isFreeze){
//                    pointService.cancelPointsFreeze(pointLogVO);
//                }
            throw bex;
        } catch (Exception e) {
//                //如果进行了积分冻结，则进行解冻
//                if(isFreeze){
//                    pointService.cancelPointsFreeze(pointLogVO);
//                }
            log.error("publish job error dto {} bo {}", dto, jobCreateBO, e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
        throw BusinessException.of(JobResponseCode.JOB_PUBLISH_FAIL);
    }


    @Override
    public InterviewResultVO createAIInterview(JobCreateBO jobCreateBO){
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

    /**
     * 更新面试
     * @param jobUpdateBO
     * @return
     */
    private InterviewUpdateResultVO updateAIInterview(JobUpdateBO jobUpdateBO){
        //更新ai面试
        InterviewUpdateRequestDTO interviewRequestDTO = new InterviewUpdateRequestDTO();
        interviewRequestDTO.setInterviewId(jobUpdateBO.getInterviewUrlId());
        interviewRequestDTO.setNewTimeDuration(jobUpdateBO.getInterviewLength());
        interviewRequestDTO.setDimensions(jobUpdateBO.getSkills());
        interviewRequestDTO.setCustomQuestions(jobUpdateBO.getCustomQuestions());
        interviewRequestDTO.setInterviewType(jobUpdateBO.getInterviewType());

        interviewRequestDTO.setEnablePersonalityTest(jobUpdateBO.getPersonalityTestEnabled());
        interviewRequestDTO.setCustomQuestionOnly(jobUpdateBO.getCustomQuestionEnabled());
        interviewRequestDTO.setEnableMidwayScoring(jobUpdateBO.getCheckpointEnabled());

        JobEntity jobEntity = jobService.getJobsByIds(jobUpdateBO.getJobId());
        return aiService.updateInterview(interviewRequestDTO,jobEntity);
    }

    @Override
    public List<LocationTypeVO> getAllLocationTypes() {
        List<JobModeDto> modeServiceAll = jobModeService.getAll();
        return modeServiceAll.stream().map(mode -> new LocationTypeVO(mode.getId(), mode.getName())).toList();
    }

    @Override
    public Pager<JobOptionVO> listJobOption(JobOptionDTO option) {
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        String companyCode = currentUserNeedLogin.getCompanyCode();
        IPage<JobEntity> jobEntityIPage = jobService.listJobSimple(option.getPageIndex(), option.getPageSize(), companyCode);
        return Pager.build(jobEntityIPage, jobConvert::toJobOption);
    }

    @Override
    public String generateInfoShareLink(String idCode, String urlCode) {
        // Build publish URL following arySharePost method pattern
        String publishURL = CommonUtils.joinInclinedRod(jobPublishUrl, jobPublishRouteInfoPrefix, idCode, urlCode);

        // Generate final share link
//        String shareLink = CommonUtils.join(publishURL, idStr) + jobPublishInfoType;
        return publishURL;
    }


    @Override
    public String generateInfoSimpleShareLink(String idCode, String urlCode) {
        // Build publish URL following arySharePost method pattern
        String publishURL = CommonUtils.joinInclinedRod(jobPublishRouteInfoPrefix, idCode, urlCode);

        // Generate final share link
//        String shareLink = CommonUtils.join(publishURL, idStr) + jobPublishInfoType;
        return publishURL;
    }

    @Override
    public String generateListShareLink(String companyCode, String companyName) {
        // Clean company name by removing special characters
        String companyNameClean = CommonUtils.cleanInputReplace(companyName, companyNameReplace);
        // Generate share link using company name
        return CommonUtils.joinInclinedRod(jobPublishUrl, jobPublishRouteListPrefix, companyCode, companyNameClean);
    }

    @Override
    public JobDetailVO getJobDetailById(Long jobId, boolean recruiter) {
        log.info("job id {}", jobId);
        if (jobId == null || jobId <= 0) {
            return null;
        }
        JobDetailDTO dto = new JobDetailDTO();
        dto.setJobId(jobId);
        // 从MySQL查询基础职位信息
        JobEntity jobEntity = jobService.getJobsByIds(dto.getJobId());
        if (jobEntity == null) {
            log.error("Job not found with id: {}", dto.getJobId());
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }
        //获取状态活跃 的职位
        // 状态码枚举
        if (!recruiter && JobStatus.ACTIVE != JobStatus.getByCode(jobEntity.getJobStatus())) {
            log.error("Job status not active: {} {}", dto.getJobId(), jobEntity);
            throw BusinessException.of(JobResponseCode.JOB_STATUS_NOT_ACTIVE);
        }
        if (recruiter) {
            IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
            if (!StringUtils.equals(currentUserNeedLogin.getCompanyCode(), jobEntity.getCompanyCode())) {
                log.warn("current user company code {}, job entity company code {}", currentUserNeedLogin.getCompanyCode(), jobEntity.getCompanyCode());
                throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
            }
        }
        // 从ES查询职位详细信息
        JobEsEntity jobEsEntity = jobEsService.getJobById(dto.getJobId());

        // 只能判断循序处理
        List<IntelligenceScoreRuleDTO> reOrder = recruiter ? getReOrder(jobEsEntity.getIntelligenceSwitch(), jobEsEntity.getScoreRules()) : null;
        jobEsEntity.setScoreRules(reOrder);

        // 封装成JobVO
        JobDetailVO jobVO = jobConvert.toJobVO(jobEntity);
        fillEs2VO(jobEsEntity, jobVO);
        //处理category
        Map<Integer, String> allJobCategoryMapping = jobCategoryService.getAllJobCategoryMapping();
        jobVO.setCategoryName(allJobCategoryMapping.getOrDefault(jobVO.getCategoryId(), jobVO.getCategoryName()));
        //处理job model
        Map<Integer, String> allJobModeMapping = jobModeService.getAllJobModeMapping();
        jobVO.setModeName(allJobModeMapping.getOrDefault(jobVO.getModeId(), jobVO.getModeName()));
        //处理type
        Map<Integer, String> allJobTypeMapping = jobTypeService.getAllJobTypeMapping();
        jobVO.setTypeName(allJobTypeMapping.getOrDefault(jobVO.getTypeId(), jobVO.getTypeName()));
        //处理字典
        Map<Long, String> dictMapping = dictionaryService.listDictMapping(List.of(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()));
        jobVO.setSalaryTypeName(dictMapping.getOrDefault(jobVO.getSalaryType() != null ? jobVO.getSalaryType().longValue() : 0L, jobVO.getSalaryTypeName()));
        //地点处理
        List<LocationValRecordDTO> locations = jobEsEntity.getLocations().stream().toList();
        List<String> placeIds = locations.stream().map(LocationValRecordDTO::getPlaceId).filter(Objects::nonNull).distinct().toList();
        List<Long> countryIds = locations.stream().map(LocationValRecordDTO::getCountryId).filter(Objects::nonNull).distinct().toList();
        List<Long> stateIds = locations.stream().map(LocationValRecordDTO::getStateId).filter(Objects::nonNull).distinct().toList();
        List<Long> cityIds = locations.stream().map(LocationValRecordDTO::getCityId).filter(Objects::nonNull).distinct().toList();

        Map<String, String> placeIdMap = locationService.listIdNameMapByPlaceIds(placeIds);
        Map<Long, String> countryIdMap = locationService.listIdNameMapByCountryIds(countryIds);
        Map<Long, String> stateIdMap = locationService.listIdNameMapByStateIds(stateIds);
        Map<Long, String> cityIdMap = locationService.listIdNameMapByCityIds(cityIds);
        List<LocationValDTO> locationValDTOS = convertNameByLanguage(jobEsEntity, countryIdMap, stateIdMap, cityIdMap, placeIdMap);
        jobVO.setLocations(convert2Locations(locationValDTOS));

        jobVO.setCompanyCode(jobEntity.getCompanyCode());
        jobVO.setSlug(generateUrlCodeService.generateUrlCodeByConfig(jobEntity.getTitle(), jobEntity.getUrlCode(), jobEsEntity.getCustomerName(), companyNameReplace));
        return jobVO;
    }

    @Override
    public JobDetailVO getJobDetailUnlimitById(Long jobId) {
        log.info("getJobDetailUnlimitById job id {}", jobId);
        if (jobId == null || jobId <= 0) {
            return null;
        }
        JobDetailDTO dto = new JobDetailDTO();
        dto.setJobId(jobId);
        // 从MySQL查询基础职位信息
        JobEntity jobEntity = jobService.getJobsByIds(dto.getJobId());
        if (jobEntity == null) {
            log.error("getJobDetailUnlimitById job not found with id: {}", dto.getJobId());
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }
        // 从ES查询职位详细信息
        JobEsEntity jobEsEntity = jobEsService.getJobById(dto.getJobId());
        jobEsEntity.setScoreRules(null);

        // 封装成JobVO
        JobDetailVO jobVO = jobConvert.toJobVO(jobEntity);
        fillEs2VO(jobEsEntity, jobVO);
        //处理category
        Map<Integer, String> allJobCategoryMapping = jobCategoryService.getAllJobCategoryMapping();
        jobVO.setCategoryName(allJobCategoryMapping.getOrDefault(jobVO.getCategoryId(), jobVO.getCategoryName()));
        //处理job model
        Map<Integer, String> allJobModeMapping = jobModeService.getAllJobModeMapping();
        jobVO.setModeName(allJobModeMapping.getOrDefault(jobVO.getModeId(), jobVO.getModeName()));
        //处理type
        Map<Integer, String> allJobTypeMapping = jobTypeService.getAllJobTypeMapping();
        jobVO.setTypeName(allJobTypeMapping.getOrDefault(jobVO.getTypeId(), jobVO.getTypeName()));
        //处理字典
        Map<Long, String> dictMapping = dictionaryService.listDictMapping(List.of(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()));
        jobVO.setSalaryTypeName(dictMapping.getOrDefault(jobVO.getSalaryType() != null ? jobVO.getSalaryType().longValue() : 0L, jobVO.getSalaryTypeName()));
        //地点处理
        List<LocationValRecordDTO> locations = jobEsEntity.getLocations().stream().toList();
        List<String> placeIds = locations.stream().map(LocationValRecordDTO::getPlaceId).filter(Objects::nonNull).distinct().toList();
        List<Long> countryIds = locations.stream().map(LocationValRecordDTO::getCountryId).filter(Objects::nonNull).distinct().toList();
        List<Long> stateIds = locations.stream().map(LocationValRecordDTO::getStateId).filter(Objects::nonNull).distinct().toList();
        List<Long> cityIds = locations.stream().map(LocationValRecordDTO::getCityId).filter(Objects::nonNull).distinct().toList();

        Map<String, String> placeIdMap = locationService.listIdNameMapByPlaceIds(placeIds);
        Map<Long, String> countryIdMap = locationService.listIdNameMapByCountryIds(countryIds);
        Map<Long, String> stateIdMap = locationService.listIdNameMapByStateIds(stateIds);
        Map<Long, String> cityIdMap = locationService.listIdNameMapByCityIds(cityIds);
        List<LocationValDTO> locationValDTOS = convertNameByLanguage(jobEsEntity, countryIdMap, stateIdMap, cityIdMap, placeIdMap);
        jobVO.setLocations(convert2Locations(locationValDTOS));
        jobVO.setCompanyCode(jobEntity.getCompanyCode());
        jobVO.setSlug(generateUrlCodeService.generateUrlCodeByConfig(jobEntity.getTitle(), jobEntity.getUrlCode(), jobEsEntity.getCustomerName(), companyNameReplace));
        return jobVO;
    }

    @Override
    public JobDetailVO getJobDetailApplicationById(Long jobId) {
        return getJobDetailById(jobId, false);
    }

    @Override
    public JobDetailVO getJobDetailRecruiterById(Long jobId) {
        return getJobDetailById(jobId, true);
    }

    @Override
    public JobDetailVO getJobDetailByIdStr(String jobCode) {
        String jobIdStr = CommonUtils.splitterStrGetLast(jobCode);
        Long jobId = shortIdGenerator.parseShortId(jobIdStr);
        if (jobId == null || jobId <= 0) {
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }
        return getJobDetailUnlimitById(jobId);
    }


    @Override
    public List<SalaryTypeDTO> getAllSalaryTypes() {
        List<DictionaryDTO> dictionaryDTOS = dictionaryService.listByType(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName());
        return dictionaryDTOS.stream().map(SalaryTypeDTO::convertFromType).toList();
    }

    @Override
    public List<CurrencyTypeDTO> getAllCurrencyTypes() {
        List<DictionaryDTO> dictionaryDTOS = dictionaryService.listByType(DictionaryEnum.REPORT.getName());
        return dictionaryDTOS.stream().map(CurrencyTypeDTO::convertFrom).toList();
    }

    @Override
    public JobUpdateResponseVO updateJob(JobUpdateDTO dto) {
        dto.checkJobRequirement();
        checkLocations(dto.getLocations());
        checkInterviewLength(dto.getInterviewType(), dto.getInterviewLength());
        checkWrittenTestForAiPhone(dto.getInterviewType(), dto.getEnableWrittenTest());
        // 验证智能评估配置
        validateIntelligenceConfig(dto.getInterviewType(), dto.getIntelligenceSwitch(), dto.getScoreRules());
        checkInterviewLength(dto.getInterviewType(), dto.getInterviewLength());
        checkWrittenTestForAiPhone(dto.getInterviewType(), dto.getEnableWrittenTest());
        // 1. 校验jobId存在
        JobEntity jobEntity = jobService.getJobsByIds(dto.getJobId());
        if (jobEntity == null) {
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }

        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        Long currentUserId = Long.valueOf(currentUserNeedLogin.getId());

        if (jobEntity.getCreateBy() == null || !Objects.equals(jobEntity.getCreateBy(), currentUserId)) {
            log.warn("User is not job creator, edit denied. jobId={} creatorId={} currentUserId={}",
                    jobEntity.getId(), jobEntity.getCreateBy(), currentUserId);
            throw BusinessException.of(JobResponseCode.JOB_NO_PERMISSION);
        }

        if (!Objects.equals(jobEntity.getJobStatus(), JobStatus.PENDING_MODIFICATION.getCode())) {
            log.warn("Job status not editable, edit denied. jobId={} currentStatus={} requiredStatus={}",
                    jobEntity.getId(), jobEntity.getJobStatus(), JobStatus.PENDING_MODIFICATION.getCode());
            throw BusinessException.of(JobResponseCode.JOB_EDIT_NOT_ALLOWED);
        }

        if (!jobEntity.getInterviewType().equals(dto.getInterviewType())) {
            // 面试类型发生变化时，检查是否有候选人申请记录，如果有则不允许修改
            if (candidateJobService.hasApplicationsByJobId(jobEntity.getId())) {
                log.warn("Cannot modify interview type for job {} because it has candidate applications",
                        jobEntity.getId());
                throw BusinessException.of(JobResponseCode.JOB_INTERVIEW_TYPE_CANNOT_MODIFY_WITH_APPLICATIONS);
            }
        }
        // 5S试题笔试是否启用 不允许编辑 产品需求
        checkQuestion5STestChange(dto, jobEntity);

        // 2. 权限校验（伪代码，实际应根据当前登录用户信息判断）
        // if (!currentUser.isAdmin() && !currentUser.getId().equals(jobEntity.getCreateBy())) {
        //     throw BusinessException.of(JobResponseCode.NO_PERMISSION);
        // }
        // 3. 使用MapStruct进行属性拷贝（只拷贝可变字段）
        JobUpdateBO jobUpdateBO = jobConvert.toJobUpdateBO(dto);
        if (jobUpdateBO.getEnableWrittenTest() == null) {
            jobUpdateBO.setEnableWrittenTest(jobEntity.getEnableWrittenTest());
        }
        jobUpdateBO.fillLocationName();

        IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(currentUserNeedLogin.getCompanyCode());
        //添加更新ai的id 用于ai数据更新
        jobUpdateBO.setInterviewUrlId(jobEntity.getInterviewUrlId());
        // 更新的人
        jobUpdateBO.setUpdateUser(currentUserNeedLogin.getUserName());
        jobUpdateBO.setUpdateBy(Long.parseLong(currentUserNeedLogin.getId()));
        jobUpdateBO.setModeName(Optional.ofNullable(getMode(jobUpdateBO.getModeId())).map(JobModeEntity::getModeName).orElse(""));
        jobUpdateBO.setTypeName(Optional.ofNullable(jobTypeService.getById(jobUpdateBO.getTypeId())).map(JobTypeEntity::getName).orElse(""));
        jobUpdateBO.setCategoryName(Optional.ofNullable(jobCategoryService.getById(jobUpdateBO.getCategoryId())).map(JobCategoryEntity::getName).orElse(""));
        jobUpdateBO.setCompanyCode(companyInfo.getCompanyCode());
        jobUpdateBO.setCustomerName(companyInfo.getCompanyName());
        jobUpdateBO.setLogoPath(companyInfo.getLogopath());
        jobUpdateBO.setCurrencyName(getCurrencyName(dto.getCurrency()));
        jobUpdateBO.setSalaryTypeName(getSalaryTypeName(dto.getSalaryType()));

        String companyName = CommonUtils.cleanInputReplace(jobUpdateBO.getCustomerName(), companyNameReplace);
        String title = CommonUtils.cleanInputReplace(jobEntity.getTitle(), companyNameReplace);
        String urlCode = CommonUtils.join(companyName, title);
        jobUpdateBO.setUrlCode(urlCode);

        // 异步创建 Place 信息
        if (CollectionUtils.isNotEmpty(jobUpdateBO.getLocations())) {
            jobUpdateBO.getLocations().stream()
                .map(LocationValDTO::getPlaceId)
                .filter(StringUtils::isNotBlank)
                .distinct()  // 去重，避免重复处理相同的 placeId
                .forEach(placeId -> {
                    locationTaskExecutor.execute(() -> {
                        try {
                            placeService.asyncCreatePlaceInfo(placeId);
                        } catch (Exception e) {
                            log.error("Failed to create place info for placeId: {}", placeId, e);
                        }
                    });
                });
        }

        // Generate hash for company name and title for uniqueness validation
        String companyTitleHash = HashUtils.murmur3Hash(urlCode);
        jobUpdateBO.setCompanyTitleHash(companyTitleHash);
        RLock lock = redissonClient.getLock(RedisKeyUtil.getEditJobKey(jobEntity.getId()));
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
                //校验岗位是否重复 重复直接返回 二次确认
                if (checkJobDuplication(jobEntity.getTitle(), jobUpdateBO.getLocations(), jobUpdateBO.getCompanyCode(), jobEntity.getId(), jobUpdateBO.isConfirmSave())) {
                    return new JobUpdateResponseVO(true);
                }
                //校验积分是否充足
                if (!pointService.updateJobCheckPoints(jobEntity.getId(),companyInfo.getCompanyCode(),jobEntity.getInterviewType(),dto.getInterviewType())){
                    throw BusinessException.of(JobResponseCode.JOB_UPDATE_INSUFFICIENT_POINTS);
                }

                // Check if job status requires special handling according to PRD Section 2.5.1
                Integer currentStatus = jobEntity.getJobStatus();
                boolean shouldPublish = false;
                if (currentStatus.equals(JobStatus.PENDING_MODIFICATION.getCode()) ||
                        currentStatus.equals(JobStatus.PENDING_PUBLICATION.getCode())) {

                    jobUpdateBO.setJobStatus(JobStatus.PENDING_REVIEW.getCode());
                    jobUpdateBO.setSubmittedForApprovalAt(LocalDateTime.now());
                } else {
                    shouldPublish = !currentStatus.equals(JobStatus.ACTIVE.getCode());
                }

                JobAuditHistoryBO audit = new JobAuditHistoryBO();
                audit.setJobId(jobUpdateBO.getJobId());
                audit.setOldStatus(currentStatus);
                if(jobUpdateBO.getJobStatus() == null) {
                    audit.setNewStatus(currentStatus);
                } else {
                    audit.setNewStatus(jobUpdateBO.getJobStatus());
                }
                audit.setAction(JobApprovalAction.RESUBMIT.toString());
                audit.setComment(null);
                audit.setCreatedBy(Long.valueOf(UserContextUtil.getCurrentUserNeedLogin().getId()));
                audit.setCreatedAt(LocalDateTime.now());
                jobUpdateBO.setJobAuditHistory(audit);
                boolean updateJob = jobDoubleDataSourceService.updateJob(jobUpdateBO);
                if (!updateJob) {
                    log.warn("update job failed {}", jobEntity);
                    throw BusinessException.of(JobResponseCode.JOB_UPDATE_FAIL);
                }

                if(shouldPublish) {
                    // 是否自动发布到其他网站 对接
//                String idStr = shortIdGenerator.generateShortId(jobEntity.getId());
//                String publishURL = CommonUtils.joinInclinedRod(jobPublishUrl, jobPublishUrlPrefix, jobUpdateBO.getUrlCode());
//                String publishURLIdStr = CommonUtils.join(publishURL, idStr);
//                log.info("publishURLIdStr {}", publishURLIdStr);
                    //TODO 增加面试时长 发送到ai服务 更新也需要通知ai
                    //生成url id 异步处理，定时任务补偿
                    updateAiInterview(jobEntity, jobUpdateBO);
                    //异步生成 feed xml
                    aiTaskExecutor.execute(() -> {
                        xmlFeedConfigService.jobUpdate(currentUserNeedLogin.getCompanyCode(),Long.valueOf(currentUserNeedLogin.getId()));
                    });
                    // Notify Naukri about updates
                    try {
                        if(StringUtils.isNotBlank(jobEntity.getNaukriJobId()))
                        {
                            naukriService.asyncUpdateJob(jobUpdateBO);
                        }
                    } catch (Exception ex) {
                        log.warn("Naukri update invoke failed jobId={} error=", jobUpdateBO.getJobId(), ex);
                    }
                } else {
                    var adminForResubmit = iamRpcAdapter.getAdminByCompanyCode(jobEntity.getCompanyCode());
                    String adminEmailForResubmit = adminForResubmit != null ? adminForResubmit.getEmail() : null;
                    jobApprovalService.sendApprovalNotification(jobEntity.getId(), JobApprovalAction.RESUBMIT.toString(), null, adminEmailForResubmit);
                }

              return new JobUpdateResponseVO(false);

            } else {
                throw BusinessException.of(COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER);
            }
        } catch (BusinessException bex) {
            log.warn("update job error dto {} bo {}", dto, jobUpdateBO, bex);
            throw bex;
        } catch (Exception e) {
            log.error("update job error dto {} bo {}", dto, jobUpdateBO, e);
        } finally {
            if  (locked) {
                lock.unlock();
            }
        }

        throw BusinessException.of(JobResponseCode.JOB_UPDATE_FAIL);
    }

    private void checkQuestion5STestChange(JobUpdateDTO dto, JobEntity jobEntity) {
        JobEsEntity jobEs = jobEsService.getJobById(jobEntity.getId(), LambdaUtil.getFieldNames(JobEsEntity::getEnableQuestion5STest));
        if (dto.getEnableQuestion5STest() != null && dto.getEnableQuestion5STest().equals(jobEs.getEnableQuestion5STest()) ||
                dto.getEnableQuestion5STest() == null && jobEs.getEnableQuestion5STest() == null ||
                dto.getEnableQuestion5STest() == null && !jobEs.getEnableQuestion5STest() ||
                jobEs.getEnableQuestion5STest() == null && !dto.getEnableQuestion5STest()) {
            return ;
        }
        throw BusinessException.of(JobResponseCode.JOB_QUESTION_5S_TEST_NOT_ALLOWED);
    }

    private void ayrSharePost(JobCreateBO jobCreateBO, IamCompanyDetailDTO companyInfo, Long jobId) {
        log.info("ayrShare jobPublishAyrShare {} jobPublishUrl {} jobPublishUrlPrefix {} companyNameReplace {}", jobPublishAyrShare, jobPublishUrl, jobPublishRouteInfoPrefix, companyNameReplace);
        if (jobPublishAyrShare && HotListType.isEnabled(jobCreateBO.getHotList())) {
            String idStr = shortIdGenerator.generateShortId(jobId);
//            String publishURL = CommonUtils.joinInclinedRod(jobPublishUrl, jobPublishRouteInfoPrefix, jobCreateBO.getUrlCode());
//            String publishURLIdStr = CommonUtils.join(publishURL, idStr) + jobPublishInfoType;
            String generateUrlCode = generateUrlCodeService.generateUrlCodeByConfig(jobCreateBO.getTitle(), jobCreateBO.getUrlCode(), companyInfo.getCompanyName(), companyNameReplace);
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
            // 使用新的CompletableFuture方法，从数据库获取配置信息并更新Job状态
            ayrShareService.asyncSendToAyrShareWithConfig(ayrSharePostDTO);
        }
    }

    @Override
    public String generateCompanyShareLink() {
        // Get master account information
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();
        String companyCode = Optional.ofNullable(currentUser).map(IamUserContextDTO::getCompanyCode).orElse(null);
        if (StringUtils.isBlank(companyCode)) {
            log.warn("companyCode is empty");
            throw BusinessException.of(GlobalStatusCode.TOKEN_INVALID,"Invalid authentication");
        }
        IamCompanyDTO companyInfoByCode = iamRpcAdapter.getCompanyInfoByCode(companyCode);
        String companyName = Optional.ofNullable(companyInfoByCode).map(IamCompanyDTO::getCompanyName).orElse("");
        // Clean company name by removing special characters
//        String companyNameClean = CommonUtils.cleanInputReplace(companyName, companyNameReplace);
//        String publishList = CommonUtils.join(companyNameClean, companyCode) + jobPublishListType;
//        // Generate share link using company name
//        String shareLink = CommonUtils.joinInclinedRod(jobPublishUrl, jobPublishRouteListPrefix, publishList);
        String shareLink = generateListShareLink(companyCode, companyName);
        log.info("Generated job list share link: {} for user info: {}", shareLink, currentUser);

        return shareLink;
    }

    @Override
    public String generateJobInfoShareLink(Long jobId) {
        log.info("Generating job info share link for jobId: {}", jobId);
//        IamUserContextDTO currentUserRecruitNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        // Validate jobId
        if (jobId == null || jobId <= 0) {
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }

        // Query job data from database
        JobEntity jobEntity = jobService.getJobsByIds(jobId);
        if (jobEntity == null) {
            log.warn("Job not found with id: {}", jobId);
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }

//        if (!Strings.CS.equals(jobEntity.getCompanyCode(), currentUserRecruitNeedLogin.getCompanyCode())) {
//            log.warn("currentUser not operating the job jobEntity {} currentUserRecruitNeedLogin {}", jobEntity, currentUserRecruitNeedLogin);
//            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
//        }

        // Get urlCode from job entity
        String urlCode = jobEntity.getUrlCode();
        if (urlCode == null || urlCode.trim().isEmpty()) {
            log.warn("Job urlCode is empty for jobId: {} {}", jobId, jobEntity);
            throw BusinessException.of(JobResponseCode.JOB_LINK_FAIL);
        }

        // Generate short ID for the job
        String idStr = shortIdGenerator.generateShortId(jobId);

        // Build publish URL following arySharePost method pattern
//        String publishURL = CommonUtils.joinInclinedRod(jobPublishUrl, jobPublishRouteInfoPrefix, urlCode);

        // Generate final share link
//        String shareLink = CommonUtils.join(publishURL, idStr) + jobPublishInfoType;
        IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(jobEntity.getCompanyCode());
        String generateUrlCode = generateUrlCodeService.generateUrlCodeByConfig(jobEntity.getTitle(), jobEntity.getUrlCode(), companyInfo.getCompanyName(), companyNameReplace);
        String shareLink = generateInfoShareLink(idStr, generateUrlCode);
        log.info("Generated job info share link: {} for jobEntity: {}", shareLink, jobEntity);

        return shareLink;
    }


    @Override
    public String generateJobInfoSimpleShareLink(Long jobId) {
        log.info("Generating job info simple share link for jobId: {}", jobId);
//        IamUserContextDTO currentUserRecruitNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        // Validate jobId
        if (jobId == null || jobId <= 0) {
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }

        // Query job data from database
        JobEntity jobEntity = jobService.getJobsByIds(jobId);
        if (jobEntity == null) {
            log.warn("Job not found with id: {}", jobId);
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }

//        if (!Strings.CS.equals(jobEntity.getCompanyCode(), currentUserRecruitNeedLogin.getCompanyCode())) {
//            log.warn("currentUser not operating the job jobEntity {} currentUserRecruitNeedLogin {}", jobEntity, currentUserRecruitNeedLogin);
//            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
//        }

        // Get urlCode from job entity
        String urlCode = jobEntity.getUrlCode();
        if (urlCode == null || urlCode.trim().isEmpty()) {
            log.warn("Job urlCode is empty for jobId: {} {}", jobId, jobEntity);
            throw BusinessException.of(JobResponseCode.JOB_LINK_FAIL);
        }

        // Generate short ID for the job
        String idStr = shortIdGenerator.generateShortId(jobId);
        IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(jobEntity.getCompanyCode());
        String generateUrlCode = generateUrlCodeService.generateUrlCodeByConfig(jobEntity.getTitle(), jobEntity.getUrlCode(), companyInfo.getCompanyName(), companyNameReplace);
        String shareLink = generateInfoSimpleShareLink(idStr, generateUrlCode);
        log.info("Generated job info simple share link: {} for jobEntity: {}", shareLink, jobEntity);

        return shareLink;
    }

    @Override
    public Boolean updateJobStatus(Long jobId, Integer jobStatus, String comment) {
        // 1. Validate jobId exists
        JobEntity jobEntity = jobService.getJobsByIds(jobId);
        if (jobEntity == null) {
            log.warn("Job not found with id: {}", jobId);
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }

        // 2. Validate status transition according to PRD Section 2.5
        JobStatus currentStatus = JobStatus.getByCode(jobEntity.getJobStatus());
        JobStatus targetStatus = JobStatus.getByCode(jobStatus);

        if (!isValidStatusTransition(currentStatus, targetStatus)) {
            log.warn("Invalid status transition from {} to {} for job {}", currentStatus, jobStatus, jobId);
            throw BusinessException.of(JobResponseCode.INVALID_STATUS_TRANSITION);
        }

        //Only MASTER account can publish (ACTIVE)
        if (targetStatus == JobStatus.ACTIVE && currentStatus != JobStatus.ON_HOLD) {
            IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserNeedLogin();
            UserIdentifyTypeEnum userIdentify = UserIdentifyTypeEnum.getByCode(currentUserNeedLogin.getUserIdentifyCode());

            if (userIdentify != UserIdentifyTypeEnum.MASTER_RECRUIT) {
                log.warn("Non-master user attempted to publish job. jobId={} companyCode={} userId={} userIdentify={}",
                        jobId,
                        jobEntity.getCompanyCode(),
                        currentUserNeedLogin.getId(),
                        userIdentify != null ? userIdentify.name() : null);

                // Reuse the auth error semantics used by AuthInterceptor
                throw BusinessException.of(AuthResponseCode.ONLY_MASTER_CAN_PUBLISH);
            }
        }

        JobApprovalAction action = switch (targetStatus) {
            case PENDING_MODIFICATION -> JobApprovalAction.RESUBMIT;
            case PENDING_PUBLICATION -> JobApprovalAction.APPROVE;
            case ACTIVE -> JobApprovalAction.PUBLISH;
            case CLOSED -> JobApprovalAction.CLOSE;
            case ON_HOLD -> JobApprovalAction.ON_HOLD;
            default -> JobApprovalAction.REJECT;
        };

        // 4. Update job status
        Integer oldStatus = jobEntity.getJobStatus();
        JobStatus oldJobStatus = JobStatus.getByCode(oldStatus);
        JobAuditHistoryBO audit = new JobAuditHistoryBO();
        audit.setJobId(jobId);
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(targetStatus.getCode());
        audit.setAction(action.toString());
        audit.setComment(null);
        audit.setCreatedBy(Long.valueOf(UserContextUtil.getCurrentUserNeedLogin().getId()));
        audit.setCreatedAt(LocalDateTime.now());
        Boolean updated = jobDoubleDataSourceService.updateJobStatus(jobId, jobStatus, audit);

        // 5. 异步生成 feed xml and notify
        if(updated){
            boolean shouldSendApprovalNotification =
                    (JobStatus.ACTIVE == oldJobStatus && JobStatus.ON_HOLD == targetStatus) || // toggle between active and on hold
                    (JobStatus.ON_HOLD == oldJobStatus && JobStatus.ACTIVE == targetStatus) || // toggle between on hold and active
                    JobStatus.CLOSED == targetStatus; // close job

            if (!shouldSendApprovalNotification) {
                String creatorEmail = iamUserClient.getUserEmailById(jobEntity.getCreateBy());
                jobApprovalService.sendApprovalNotification(jobId, action.toString(), comment, creatorEmail);
            }

            IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
            aiTaskExecutor.execute(() -> {
                xmlFeedConfigService.jobUpdate(currentUserNeedLogin.getCompanyCode(),Long.valueOf(currentUserNeedLogin.getId()));
            });
        }
        // If job is closed/unpublished, notify Naukri
        try {
            log.info("Naukri unpublish invoke jobId={} updated={} jobStatus={}", jobId, updated, jobStatus);
            if (Boolean.TRUE.equals(updated) && JobStatus.CLOSED == JobStatus.getByCode(jobStatus) && StringUtils.isNotBlank(jobEntity.getNaukriJobId())) {
                naukriService.asyncUnpublishJob(jobEntity.getNaukriJobId());
            }
        } catch (Exception ex) {
            log.warn("Naukri unpublish invoke failed jobId={} error=", jobId, ex);
        }
        return updated;
    }

    @Override
    public Boolean deleteJob(Long jobId) {
        // 1. Validate jobId exists
        JobEntity jobEntity = jobService.getJobsByIds(jobId);
        if (jobEntity == null) {
            log.warn("Job not found with id: {}", jobId);
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }

        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        if (!StringUtils.equals(currentUserNeedLogin.getCompanyCode(), jobEntity.getCompanyCode())) {
            log.warn("currentUserNeedLogin  companyCode  {}, jobEntity companyCode {} ", currentUserNeedLogin.getCompanyCode(), jobEntity.getCompanyCode());
            throw BusinessException.of(GlobalStatusCode.TOKEN_INVALID,"Invalid authentication");
        }

        // 2. Permission validation (placeholder for future implementation)
        // if (!currentUser.isAdmin() && !currentUser.getId().equals(jobEntity.getCreateBy())) {
        //     throw BusinessException.of(JobResponseCode.NO_PERMISSION);
        // }

        // 3. Delete job from both DB and ES
        try {
            boolean deleteResult = jobDoubleDataSourceService.deleteJob(jobId);
            if (deleteResult) {
                log.info("Successfully deleted job with id: {}", jobId);
                //异步生成 feed xml
                aiTaskExecutor.execute(() -> {
                    xmlFeedConfigService.jobUpdate(currentUserNeedLogin.getCompanyCode(),Long.valueOf(currentUserNeedLogin.getId()));
                });
            } else {
                log.warn("Failed to delete job with id: {}", jobId);
            }
            return deleteResult;
        } catch (Exception e) {
            log.error("Error deleting job with id: {}", jobId, e);
            throw BusinessException.of(JobResponseCode.JOB_DELETE_FAIL);
        }
    }

    @Override
    public Pager<JobListVO> searchJobsFromEs(int pageNo, int pageSize, String keyword, JobDto job, Integer jobPosted) {
        try {
            // ========== 新增：地理位置参数校验 ==========
            if (job.getUserLatitude() != null || job.getUserLongitude() != null) {
                validateGeoParams(job);
            }

            var results = jobEsService.searchJobs(keyword, pageNo, pageSize, job, jobPosted);
            Set<Long> jobIds = results.getCurrentPageRecords().stream().map(JobEsEntity::getId).collect(Collectors.toSet());
            Map<Long, Long> jobApplicationCount = candidateJobService.batchApplicationCount(jobIds);
            //处理category
            Map<Integer, String> categoryMapping = jobCategoryService.getAllJobCategoryMapping();
            //处理job model
            Map<Integer, String> allJobModeMapping = jobModeService.getAllJobModeMapping();
            //处理type
            Map<Integer, String> allJobTypeMapping = jobTypeService.getAllJobTypeMapping();
            //处理字典
            Map<Long, String> dictMapping = dictionaryService.listDictMapping(List.of(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()));

            //地点处理
            List<LocationValRecordDTO> locations = results.getCurrentPageRecords().stream().flatMap(s -> s.getLocations().stream()).toList();
            List<String> placeIds = locations.stream().map(LocationValRecordDTO::getPlaceId).filter(Objects::nonNull).distinct().toList();
            List<Long> countryIds = locations.stream().map(LocationValRecordDTO::getCountryId).filter(Objects::nonNull).distinct().toList();
            List<Long> stateIds = locations.stream().map(LocationValRecordDTO::getStateId).filter(Objects::nonNull).distinct().toList();
            List<Long> cityIds = locations.stream().map(LocationValRecordDTO::getCityId).filter(Objects::nonNull).distinct().toList();

            Map<String, String> placeIdMap = locationService.listIdNameMapByPlaceIds(placeIds);
            Map<Long, String> countryIdMap = locationService.listIdNameMapByCountryIds(countryIds);
            Map<Long, String> stateIdMap = locationService.listIdNameMapByStateIds(stateIds);
            Map<Long, String> cityIdMap = locationService.listIdNameMapByCityIds(cityIds);

            List<JobListVO> jobList = new ArrayList<>();
            results.getCurrentPageRecords().forEach(r -> {
                JobListVO vo = JobConvert.INSTANCE.converToJobListVO(r);
                vo.setApplicationCounts(jobApplicationCount.getOrDefault(vo.getJobId(), 0L));
                String companyCode = r.getCompanyCode();
                fillCompanyInfo(companyCode, vo);
                vo.setCategoryName(categoryMapping.getOrDefault(vo.getCategoryId(), vo.getCategoryName()));
                vo.setModeName(allJobModeMapping.getOrDefault(vo.getModeId(), vo.getModeName()));
                vo.setTypeName(allJobTypeMapping.getOrDefault(vo.getTypeId(), vo.getTypeName()));
                vo.setSalaryTypeName(dictMapping.getOrDefault(vo.getSalaryType()!=null ? vo.getSalaryType().longValue() : 0L, vo.getSalaryTypeName()));
                if (r.getInterviewType()==null){
                    vo.setInterviewType(InterviewTypeEnum.VIDEO.getCode());
                }

                // ========== 新增：计算并设置距离信息 ==========
                if (job.getUserLatitude() != null && job.getUserLongitude() != null) {
                    String distance = calculateDistance(r, job.getUserLatitude(), job.getUserLongitude());
                    vo.setDistance(distance);
                    vo.setDistanceUnit("km");
                }

                vo.setLocations(convertNameByLanguage(r, countryIdMap, stateIdMap, cityIdMap, placeIdMap));            
                if (r.getCreateBy() == null) {
                    var jobEntity = jobService.getJobsByIds(vo.getJobId());
                    vo.setCreateUserId(jobEntity.getCreateBy());
                }
                jobList.add(vo);
            });

            // ========== 新增：应用层按距离排序 ==========
            // 由于 ES Java Client 8.13.4 不支持嵌套字段的 geo_distance 排序
            // 我们在应用层进行排序
            if (job.getUserLatitude() != null
                && job.getUserLongitude() != null
                && Boolean.TRUE.equals(job.getSortByDistance())) {
                jobList.sort((j1, j2) -> {
                    if (j1.getDistance() == null && j2.getDistance() == null) return 0;
                    if (j1.getDistance() == null) return 1;
                    if (j2.getDistance() == null) return -1;
                    try {
                        double d1 = Double.parseDouble(j1.getDistance());
                        double d2 = Double.parseDouble(j2.getDistance());
                        return Double.compare(d1, d2);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });
                log.debug("Applied distance sorting in application layer");
            }

            Pager<JobListVO> pager = new Pager<>();
            pager.setCurrentPageRecords(jobList);
            pager.setPageIndex(results.getPageIndex());
            pager.setPageSize(results.getPageSize());
            pager.setTotalCount(results.getTotalCount());

            return pager;
        } catch (Exception ex) {
            log.error("Error in searchJobsFromEs", ex);
            return null;
        }
    }

    private void fillCompanyInfo(String companyCode, JobListVO vo) {
        // Get company info using LoadingCache
        if (companyCode != null) {
            CompanyInfoSimpleDTO companyInfo = companyInfoLoadingCache.get(companyCode);
            // Set company information to JobListVO using existing fields
            if (companyInfo != null) {
                // Use existing customerName field for company name
                if (StringUtils.isNotBlank(companyInfo.getName())) {
                    vo.setCompanyName(companyInfo.getName());
                }
                // Set logo URL using existing logoUrl field
                if (StringUtils.isNotBlank(companyInfo.getLogo())) {
                    vo.setLogoUrl(companyInfo.getLogo());
                }
                // Set logo URL using existing logoUrl field
                if (StringUtils.isNotBlank(companyInfo.getWebsite())) {
                    vo.setCompanyWebsite(companyInfo.getWebsite());
                }
            }
        }
    }

    private String logoPath2Url(String logoPath){
        if (StringUtils.isBlank(logoPath)) {
            return null;
        }
        try {
            return s3Utils.generatePresignedUrl(logoPath);
        } catch (Exception e) {
            log.error("logoPath2Url logo path {}", logoPath, e);
        }
        return null;
    }

    /**
     * 将es中数据填充到VO中
     * @param jobEsEntity
     * @param jobVO
     */
    private void fillEs2VO(JobEsEntity jobEsEntity, JobDetailVO jobVO) {
        if (jobEsEntity != null) {
            // 如果ES中有数据，补充ES中的详细信息
            jobVO.setJobDetail(jobEsEntity.getJobDetail());
            jobVO.setMinimumJobRequirement(jobEsEntity.getMinimumJobRequirement());
            jobVO.setPreferredJobRequirement(jobEsEntity.getPreferredJobRequirement());
            jobVO.setMainDuty(jobEsEntity.getMainDuty());
            jobVO.setSkills(jobEsEntity.getSkills());
            jobVO.setCustomQuestions(jobEsEntity.getCustomQuestions());
            jobVO.setBenefits(jobEsEntity.getBenefits());
            jobVO.setLocationName(jobEsEntity.getLocationName());
            jobVO.setLogoPath(jobEsEntity.getLogoPath());
            jobVO.setTypeName(jobEsEntity.getTypeName());
            jobVO.setModeName(jobEsEntity.getModeName());
            jobVO.setSalaryTypeName(jobEsEntity.getSalaryTypeName());
            jobVO.setCategoryName(jobEsEntity.getCategoryName());
            jobVO.setCurrencySimpleDesc(jobEsEntity.getCurrencyName());
            jobVO.setLocations(convertLocations(jobEsEntity.getLocations()));
            jobVO.setEnableQuestion5STest(jobEsEntity.getEnableQuestion5STest());
            if(jobEsEntity.getInterviewType()==null){
                jobVO.setInterviewType(InterviewTypeEnum.VIDEO.getCode());
            }else{
                jobVO.setInterviewType(jobEsEntity.getInterviewType());
            }
            jobVO.setIntelligenceSwitch(jobEsEntity.getIntelligenceSwitch());
            jobVO.setScoreRules(jobConvert.toIntelligenceScoreRuleVOList(jobEsEntity.getScoreRules()));
            jobVO.setCheckpointEnabled(jobEsEntity.getCheckpointEnabled());
            jobVO.setCheckpointTimeMinutes(jobEsEntity.getCheckpointTimeMinutes());
            jobVO.setCheckpointScoreThreshold(jobEsEntity.getCheckpointScoreThreshold());
            jobVO.setPersonalityTestEnabled(jobEsEntity.getPersonalityTestEnabled());
            jobVO.setCustomQuestionEnabled(jobEsEntity.getCustomQuestionEnabled());
        }
    }

    private String getCurrencyName(Integer currency) {
        if (currency == null) {
            return "";
        }
        List<DictionaryDTO> dictionaryDTOS = dictionaryService.listByType(DictionaryEnum.REPORT.getName());
        if (CollectionUtils.isEmpty(dictionaryDTOS)) {
            return "";
        }
        DictionaryDTO dictionaryDTO = dictionaryDTOS.stream()
                .filter(d -> d.getId().intValue() == currency)
                .findFirst().orElse(null);
        if (dictionaryDTO == null) {
            log.warn("job currency not exist {} ", currency);
            throw BusinessException.of(JobResponseCode.JOB_CURRENCY_NOT_FOUND);
        }
        return dictionaryDTO.getCode();
    }

    private String getSalaryTypeName(Integer salary) {
        if (salary == null) {
            throw BusinessException.of(JOB_SALARY_TYPE_NOT_FOUND);
        }
        List<DictionaryDTO> salaryDTOS = dictionaryService.listByType(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName());
        if (CollectionUtils.isEmpty(salaryDTOS)) {
            throw BusinessException.of(JOB_SALARY_TYPE_NOT_FOUND);
        }
        DictionaryDTO salaryDTO = salaryDTOS.stream()
                .filter(d -> d.getId().intValue() == salary)
                .findFirst().orElse(null);
        if (salaryDTO == null) {
            log.warn("job salary not exist {} ", salaryDTO);
            throw BusinessException.of(JOB_SALARY_TYPE_NOT_FOUND);
        }
        return salaryDTO.getValue();
    }

    private JobModeEntity getMode(Integer modeId) {
        if (modeId == null) {
            throw BusinessException.of(JOB_LOCATION_TYPE_NOT_FOUND);
        }
        JobModeEntity mode = jobModeService.getById(modeId);
        if (mode == null) {
            throw BusinessException.of(JOB_LOCATION_TYPE_NOT_FOUND);
        }
        return mode;
    }

    public List<LocationDTO> convertLocations(List<LocationValRecordDTO> locations) {
        if (CollectionUtils.isNotEmpty(locations)) {
            return locations.stream().map(lr-> {
                LocationDTO locationDTO = new LocationDTO();
                locationDTO.setLocationName(lr.getLocationName());
                locationDTO.setCountryId(lr.getCountryId());
                locationDTO.setCountryName(lr.getCountryName());
                locationDTO.setCityId(lr.getCityId());
                locationDTO.setCityName(lr.getCityName());
                locationDTO.setStateId(lr.getStateId());
                locationDTO.setStateName(lr.getStateName());
                if (lr.getGeoPoint()!=null) {
                    double lat = lr.getGeoPoint().latlon().lat();
                    double lon = lr.getGeoPoint().latlon().lon();
                    GeoPointDTO geoPointDTO = new GeoPointDTO();
                    geoPointDTO.setLatitude(lat);
                    geoPointDTO.setLongitude(lon);
                    locationDTO.setGeoPoint(geoPointDTO);
                }
                return locationDTO;
            }).toList();
        }
        return null;
    }

    private void checkLocations(List<LocationValDTO> locations) {
        if (CollectionUtils.isEmpty(locations)) {

            throw BusinessException.of(JobResponseCode.JOB_LOCATION_IS_NULL);
        }
        List<String> placeIds = locations.stream()
                .map(LocationValDTO::getPlaceId)
                .filter(StringUtils::isNotEmpty).distinct().toList();
        List<Long> countryIds = locations.stream().filter((item) -> StringUtils.isEmpty(item.getPlaceId())).map(LocationValDTO::getCountryId).distinct().toList();
        List<Long> stateIds = locations.stream().filter((item) -> StringUtils.isEmpty(item.getPlaceId())).map(LocationValDTO::getStateId).distinct().toList();
        List<Long> cityIds = locations.stream().filter((item) -> StringUtils.isEmpty(item.getPlaceId())).map(LocationValDTO::getCityId).distinct().toList();
        if (CollectionUtils.isEmpty(countryIds) && CollectionUtils.isEmpty(stateIds) && CollectionUtils.isEmpty(cityIds)) {
            if (CollectionUtils.isEmpty(placeIds)) {
                throw BusinessException.of(JobResponseCode.JOB_LOCATION_DATA_INVALID);
            } else {
                return;
            }
        }
        //批量查询国家、省、城市
        List<CountryDTO> countryDTOS = locationService.listByCountryIds(countryIds);
        if (!CommonUtils.isCollectionSizeEquals(countryDTOS, countryIds)) {
            throw BusinessException.of(JobResponseCode.JOB_LOCATION_DATA_INVALID);
        }
        List<StateDTO> stateDTOS = locationService.listByStateIds(stateIds);
        if (!CommonUtils.isCollectionSizeEquals(stateDTOS, stateIds)) {
            throw BusinessException.of(JobResponseCode.JOB_LOCATION_DATA_INVALID);
        }
        List<CityDTO> cityDTOS = locationService.listByCityIds(cityIds);
        if (!CommonUtils.isCollectionSizeEquals(cityDTOS, cityIds)) {
            throw BusinessException.of(JobResponseCode.JOB_LOCATION_DATA_INVALID);
        }
    }

    private void checkInterviewLength(Integer interviewType, Integer interviewLength) {
        // 当 interviewType = 0 (video interview) 时，interviewLength 不能小于 10
        if (interviewType != null && interviewType == InterviewTypeEnum.VIDEO.getCode()) {
            if (interviewLength != null && interviewLength < 10) {
                throw BusinessException.of(JobResponseCode.JOB_INTERVIEW_LENGTH_INVALID);
            }
        }
    }

    private void checkWrittenTestForAiPhone(Integer interviewType, Boolean enableWrittenTest) {
        // 当 interviewType = 2 (AI_AUDIO) 时，不允许启用笔试
        if (interviewType != null && interviewType == InterviewTypeEnum.AI_PHONE.getCode()) {
            if (enableWrittenTest != null && enableWrittenTest) {
                throw BusinessException.of(JobResponseCode.JOB_AI_AUDIO_WRITTEN_TEST_NOT_ALLOWED);
            }
        }
    }

    private void updateAiInterview(JobEntity jobEntity, JobUpdateBO jobUpdateBO) {
//        if (jobEntity.getInterviewLength().equals(jobUpdateBO.getInterviewLength())) {
//            log.info("updateAiInterview edit interview length old {} new {} jobEntity {} jobUpdateBO {}", jobEntity.getInterviewLength(), jobUpdateBO.getInterviewLength(), jobEntity, jobUpdateBO);
//            return;
//        }
        aiTaskExecutor.execute(() -> {
            long start = System.currentTimeMillis();
            InterviewUpdateResultVO interviewResultVO = this.updateAIInterview(jobUpdateBO);
            log.info("Update AIInterview jobId:{},time:{},结果:{}", jobUpdateBO.getJobId(), System.currentTimeMillis() - start, interviewResultVO);
        });
        
        // Update interview type if it has changed
        if (jobEntity.getInterviewType() != null && jobUpdateBO.getInterviewType() != null 
            && !jobEntity.getInterviewType().equals(jobUpdateBO.getInterviewType())) {
            aiTaskExecutor.execute(() -> {
                try {
                    long start = System.currentTimeMillis();
                    log.info("Interview type changed from {} to {} for jobId: {}", 
                            jobEntity.getInterviewType(), jobUpdateBO.getInterviewType(), jobUpdateBO.getJobId());
                    
                    // Create update interview type request
                    UpdateInterviewTypeDTO updateRequest = new UpdateInterviewTypeDTO();
                    updateRequest.setInterviewId(jobEntity.getInterviewUrlId());
                    updateRequest.setAction("update");
                    updateRequest.setInterviewType(InterviewTypeEnum.getByCode(jobUpdateBO.getInterviewType()).getName());
                    
                    // Call AI service to update interview type
                    UpdateInterviewTypeResponseVO result = aiService.updateInterviewType(updateRequest);
                    
                    log.info("Interview type updated successfully for jobId: {}, interviewId: {}, newType: {}, time: {}ms", 
                            jobUpdateBO.getJobId(), result.getId(), result.getInterviewType(), 
                            System.currentTimeMillis() - start);
                            
                } catch (Exception e) {
                    log.error("Failed to update interview type for jobId: {}, interviewId: {}, error: {}", 
                            jobUpdateBO.getJobId(), jobEntity.getInterviewUrlId(), e.getMessage(), e);
                }
            });
        }
    }

    @Override
    public Pager<JobOptionVO> getHistoryJobList(JobHistoryListRequestDTO requestDTO) {
        // 获取当前登录用户信息
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();
        String companyCode = currentUser.getCompanyCode();
        
        // 调用ES服务进行去重搜索
        return jobEsService.searchHistoryJobsWithDeduplication(
                requestDTO.getKeyword(),
                requestDTO.getPageIndex(),
                requestDTO.getPageSize(),
                companyCode
        );
    }

    @Override
    public JobHistoryDetailVO getHistoryJobDetail(Long jobId) {
        // 获取职位详细信息
        log.info("getHistoryJobDetail jobId{}", jobId);
        if (jobId == null || jobId <= 0) {
            return null;
        }
        // 从MySQL查询基础职位信息
        JobEntity jobEntity = jobService.getJobsByIds(jobId);
        if (jobEntity == null) {
            log.error("getHistoryJobDetail Job not found with id: {} ", jobId);
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }

        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        if (!StringUtils.equals(currentUserNeedLogin.getCompanyCode(), jobEntity.getCompanyCode())) {
            log.warn("getHistoryJobDetail current user company code {}, job entity company code {}", currentUserNeedLogin.getCompanyCode(), jobEntity.getCompanyCode());
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }
        // 从ES查询职位详细信息
        JobEsEntity jobEsEntity = jobEsService.getJobById(jobId);
        List<IntelligenceScoreRuleDTO> reOrder = getReOrder(jobEsEntity.getIntelligenceSwitch(), jobEsEntity.getScoreRules());
        jobEsEntity.setScoreRules(reOrder);

        // 封装成JobVO
        JobHistoryDetailVO jobHistoryDetail = jobConvert.toJobHistoryDetail(jobEsEntity);
        jobHistoryDetail.setIntelligenceSwitch(jobEsEntity.getIntelligenceSwitch());
        jobHistoryDetail.setScoreRules(jobConvert.toIntelligenceScoreRuleVOList(jobEsEntity.getScoreRules()));

        //处理category
        Map<Integer, String> allJobCategoryMapping = jobCategoryService.getAllJobCategoryMapping();
        jobHistoryDetail.setCategoryName(allJobCategoryMapping.getOrDefault(jobHistoryDetail.getCategoryId(), jobHistoryDetail.getCategoryName()));
        //处理job model
        Map<Integer, String> allJobModeMapping = jobModeService.getAllJobModeMapping();
        jobHistoryDetail.setModeName(allJobModeMapping.getOrDefault(jobHistoryDetail.getModeId(), jobHistoryDetail.getModeName()));
        //处理type
        Map<Integer, String> allJobTypeMapping = jobTypeService.getAllJobTypeMapping();
        jobHistoryDetail.setTypeName(allJobTypeMapping.getOrDefault(jobHistoryDetail.getTypeId(), jobHistoryDetail.getTypeName()));
        //处理字典
        Map<Long, String> dictMapping = dictionaryService.listDictMapping(List.of(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()));
        //地点处理
        List<LocationValRecordDTO> locations = jobEsEntity.getLocations().stream().toList();
        List<String> placeIds = locations.stream().map(LocationValRecordDTO::getPlaceId).filter(Objects::nonNull).distinct().toList();
        List<Long> countryIds = locations.stream().map(LocationValRecordDTO::getCountryId).filter(Objects::nonNull).distinct().toList();
        List<Long> stateIds = locations.stream().map(LocationValRecordDTO::getStateId).filter(Objects::nonNull).distinct().toList();
        List<Long> cityIds = locations.stream().map(LocationValRecordDTO::getCityId).filter(Objects::nonNull).distinct().toList();

        Map<String, String> placeIdMap = locationService.listIdNameMapByPlaceIds(placeIds);
        Map<Long, String> countryIdMap = locationService.listIdNameMapByCountryIds(countryIds);
        Map<Long, String> stateIdMap = locationService.listIdNameMapByStateIds(stateIds);
        Map<Long, String> cityIdMap = locationService.listIdNameMapByCityIds(cityIds);


        jobHistoryDetail.setSalaryTypeName(dictMapping.getOrDefault(jobHistoryDetail.getSalaryType() != null ? jobHistoryDetail.getSalaryType().longValue() : 0L, jobHistoryDetail.getSalaryTypeName()));
        jobHistoryDetail.setEnableWrittenTest(jobEntity.getEnableWrittenTest());
        List<LocationValDTO> locationValDTOS = convertNameByLanguage(jobEsEntity, countryIdMap, stateIdMap, cityIdMap, placeIdMap);
        List<LocationDTO> los = convert2Locations(locationValDTOS);
        jobHistoryDetail.setLocations(los);
        //处理面试检查检查时间相关配置
        if (jobEsEntity.getCheckpointEnabled() != null && jobEsEntity.getCheckpointEnabled()) {
            jobHistoryDetail.setCheckpointEnabled(true);
            jobHistoryDetail.setCheckpointScoreThreshold(jobEsEntity.getCheckpointScoreThreshold());
            jobHistoryDetail.setCheckpointTimeMinutes(jobEsEntity.getCheckpointTimeMinutes());
        } else {
            jobHistoryDetail.setCheckpointEnabled(false);
            jobHistoryDetail.setCheckpointScoreThreshold(null);
            jobHistoryDetail.setCheckpointTimeMinutes(null);
        }
        return jobHistoryDetail;
    }

    @Override
    public boolean checkJobDuplication(String title, List<LocationValDTO> locations, String companyCode, Long excludeJobId, boolean confirmSave) {
        try {
            // 如果用户已确认保存，返回false（允许保存）不在查看是否存在 减少网络以及查询开销
            if (confirmSave) {
                return false;
            }

            //如果用户没有点击确认 需要查看是否存在重复的job信息
            List<JobEsEntity> duplicateJobs = jobEsService.searchJobsByTitleAndLocations(
                title, locations, companyCode, excludeJobId, 1);
            
            // 如果没有重复，返回false
            if (CollectionUtils.isEmpty(duplicateJobs)) {
                return false;
            }
            // 有重复且用户未确认，返回true（需要用户确认）
            return true;
        } catch (Exception e) {
            log.error("Failed to check job duplication for title: {}, locations size: {}", 
                title, locations != null ? locations.size() : 0, e);
            // 检查失败时允许保存，避免阻塞业务流程
            return false;
        }
    }

    @Override
    public List<JobListVO> getRandomJobRecommendations(RandomJobRecommendRequestDTO request) {
        try {
            // Input validation
            if (request == null || request.getSize() == null || request.getSize() <= 0) {
                log.warn("Invalid random job recommendation request: {}", request);
                return Collections.emptyList();
            }

            log.info("Getting random job recommendations with request: size={}, companyCode={}, categoryIds={}, locations={}, seed={}",
                    request.getSize(), request.getCompanyCode(), 
                    request.getCategoryId(),
                    request.getLocations() != null ? request.getLocations().size() : 0,
                    request.getSeed());
            List<Long> categoryIds = null;
            if (request.getCategoryId() != null) {
                categoryIds = List.of(request.getCategoryId());
            }
            Set<Long> excludeJobIds = null;
            if (request.getExcludeJobId() != null) {
                excludeJobIds = Set.of(request.getExcludeJobId());
            }
            // Call ES service to get random jobs
            List<JobEsEntity> randomJobs = jobEsService.searchRandomJobs(
                    request.getSize(),
                    request.getCompanyCode(),
                    request.getSeed(),
                    categoryIds,
                    request.getLocations(),
                    excludeJobIds
            );

            if (CollectionUtils.isEmpty(randomJobs)) {
                log.info("No random jobs found for request: {}", request);
                return Collections.emptyList();
            }

            // Convert JobEsEntity to JobListVO (reuse existing conversion logic)
            Set<Long> jobIds = randomJobs.stream().map(JobEsEntity::getId).collect(Collectors.toSet());
            Map<Long, Long> jobApplicationCount = candidateJobService.batchApplicationCount(jobIds);

            List<JobListVO> jobList = new ArrayList<>();
            //处理category
            Map<Integer, String> allJobCategoryMapping = jobCategoryService.getAllJobCategoryMapping();
            //处理job model
            Map<Integer, String> allJobModeMapping = jobModeService.getAllJobModeMapping();
            //处理type
            Map<Integer, String> allJobTypeMapping = jobTypeService.getAllJobTypeMapping();
            //处理字典
            Map<Long, String> dictMapping = dictionaryService.listDictMapping(List.of(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()));
            //地点处理
            List<LocationValRecordDTO> locations = randomJobs.stream().flatMap(s -> s.getLocations().stream()).toList();
            List<String> placeIds = locations.stream().map(LocationValRecordDTO::getPlaceId).filter(Objects::nonNull).distinct().toList();
            List<Long> countryIds = locations.stream().map(LocationValRecordDTO::getCountryId).filter(Objects::nonNull).distinct().toList();
            List<Long> stateIds = locations.stream().map(LocationValRecordDTO::getStateId).filter(Objects::nonNull).distinct().toList();
            List<Long> cityIds = locations.stream().map(LocationValRecordDTO::getCityId).filter(Objects::nonNull).distinct().toList();

            Map<String, String> placeIdMap = locationService.listIdNameMapByPlaceIds(placeIds);
            Map<Long, String> countryIdMap = locationService.listIdNameMapByCountryIds(countryIds);
            Map<Long, String> stateIdMap = locationService.listIdNameMapByStateIds(stateIds);
            Map<Long, String> cityIdMap = locationService.listIdNameMapByCityIds(cityIds);

            randomJobs.forEach(jobEsEntity -> {
                JobListVO vo = JobConvert.INSTANCE.converToJobListVO(jobEsEntity);
                vo.setApplicationCounts(jobApplicationCount.getOrDefault(vo.getJobId(), 0L));
                
                // Fill company information
                String companyCode = jobEsEntity.getCompanyCode();
                fillCompanyInfo(companyCode, vo);
                vo.setCategoryName(allJobCategoryMapping.getOrDefault(vo.getCategoryId(), vo.getCategoryName()));
                vo.setModeName(allJobModeMapping.getOrDefault(vo.getModeId(), vo.getModeName()));
                vo.setTypeName(allJobTypeMapping.getOrDefault(vo.getTypeId(), vo.getTypeName()));
                vo.setSalaryTypeName(dictMapping.getOrDefault(vo.getSalaryType()!=null ? vo.getSalaryType().longValue() : 0L, vo.getSalaryTypeName()));
                vo.setLocations(convertNameByLanguage(jobEsEntity, countryIdMap, stateIdMap, cityIdMap, placeIdMap));
                jobList.add(vo);
            });

            log.info("Successfully retrieved {} random job recommendations", jobList.size());
            return jobList;
        } catch (Exception e) {
            log.error("Failed to get random job recommendations for request: {}", request, e);
            throw BusinessException.of(JobResponseCode.JOB_SEARCH_FAIL);
        }
    }

    /**
     * Validate status transitions according to PRD Section 2.5
     */
    private boolean isValidStatusTransition(JobStatus currentStatus, JobStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return false;
        }

        return switch (currentStatus) {
            case DRAFT -> targetStatus == JobStatus.PENDING_REVIEW || targetStatus == JobStatus.ACTIVE;
            case PENDING_REVIEW -> targetStatus == JobStatus.PENDING_PUBLICATION ||
                    targetStatus == JobStatus.PENDING_MODIFICATION ||
                    targetStatus == JobStatus.DRAFT;
            case PENDING_PUBLICATION -> targetStatus == JobStatus.ACTIVE || targetStatus == JobStatus.DRAFT;
            case PENDING_MODIFICATION -> targetStatus == JobStatus.PENDING_REVIEW || targetStatus == JobStatus.DRAFT;
            case ACTIVE -> targetStatus == JobStatus.ON_HOLD || targetStatus == JobStatus.CLOSED;
            case ON_HOLD -> targetStatus == JobStatus.ACTIVE || targetStatus == JobStatus.CLOSED;
            case CLOSED -> true;
            default -> false;
        };
    }

    /**
     * 验证地理位置参数
     */
    private void validateGeoParams(JobDto job) {
        if (job.getUserLatitude() == null || job.getUserLongitude() == null) {
            throw BusinessException.of(JobResponseCode.COORDINATES_REQUIRED);
        }
        if (job.getUserLatitude() < -90 || job.getUserLatitude() > 90) {
            throw BusinessException.of(JobResponseCode.LATITUDE_OUT_OF_RANGE);
        }
        if (job.getUserLongitude() < -180 || job.getUserLongitude() > 180) {
            throw BusinessException.of(JobResponseCode.LONGITUDE_OUT_OF_RANGE);
        }
    }

    /**
     * 计算职位到用户位置的最近距离
     */
    private String calculateDistance(JobEsEntity job, Double userLat, Double userLon) {
        if (CollectionUtils.isEmpty(job.getLocations())) {
            return null;
        }

        // 计算到最近位置的距离
        double minDistance = Double.MAX_VALUE;
        for (LocationValRecordDTO location : job.getLocations()) {
            GeoLocation geoPoint = location.getGeoPoint();
            if (geoPoint != null && geoPoint.latlon() != null) {
                double lat = geoPoint.latlon().lat();
                double lon = geoPoint.latlon().lon();
                double distance = haversineDistance(
                    userLat, userLon,
                    lat, lon
                );
                minDistance = Math.min(minDistance, distance);
            }
        }

        return minDistance == Double.MAX_VALUE ? null : String.format("%.2f", minDistance);
    }

    /**
     * Haversine 公式计算两点距离（单位：公里）
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // 地球半径（公里）

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }


    private List<LocationValDTO> convertNameByLanguage(JobEsEntity r, Map<Long, String> countryIdMap, Map<Long, String> stateIdMap, Map<Long, String> cityIdMap, Map<String, String> placeIdMap) {
        return r.getLocations().stream().map(location -> {
            LocationValDTO locationValDTO = new LocationValDTO();
            locationValDTO.setCountryId(location.getCountryId());
            locationValDTO.setCountryName(countryIdMap.getOrDefault(location.getCountryId(), location.getCountryName()));

            locationValDTO.setStateId(location.getStateId());
            locationValDTO.setStateName(stateIdMap.getOrDefault(location.getStateId(), location.getStateName()));

            locationValDTO.setCityId(location.getCityId());
            locationValDTO.setCityName(cityIdMap.getOrDefault(location.getCityId(), location.getCityName()));

            locationValDTO.setPlaceId(location.getPlaceId());
            locationValDTO.setPlaceName(placeIdMap.getOrDefault(location.getPlaceId(), location.getPlaceName()));

            GeoLocation geoPoint = location.getGeoPoint();
            GeoPointDTO geoLocation = null;
            if (geoPoint != null) {
                geoLocation = new GeoPointDTO();
                geoLocation.setLatitude(geoPoint.latlon().lat());
                geoLocation.setLongitude(geoPoint.latlon().lon());
            }
            locationValDTO.setGeoPoint(geoLocation);
            locationValDTO.setLocationName(CommonUtils.getLocationName(locationValDTO.getCountryName(), locationValDTO.getStateName(), locationValDTO.getCityName()));
            locationValDTO.replaceLocationName();
            return locationValDTO;
        }).toList();
    }

    private List<LocationDTO> convert2Locations(List<LocationValDTO> locationValDTOS) {
        List<LocationDTO> los = locationValDTOS.stream().map(l -> {
            LocationDTO locationDTO = new LocationDTO();
            locationDTO.setCountryId(l.getCountryId());
            locationDTO.setStateId(l.getStateId());
            locationDTO.setCityId(l.getCityId());
            locationDTO.setCountryName(l.getCountryName());
            locationDTO.setStateName(l.getStateName());
            locationDTO.setCityName(l.getCityName());
            locationDTO.setLocationName(l.getLocationName());
            locationDTO.setGeoPoint(l.getGeoPoint());
            locationDTO.setPlaceId(l.getPlaceId());
            locationDTO.setPlaceName(l.getPlaceName());
            return locationDTO;
        }).toList();
        return los;
    }

    /**
     * 验证智能评估配置
     * 当 intelligenceSwitch=true 时，scoreRules 不能为空
     *
     * @param intelligenceSwitch 智能判定开关
     * @param scoreRules 智能评分规则列表
     */
    private void validateIntelligenceConfig(Integer interviewType, Boolean intelligenceSwitch, List<IntelligenceScoreRuleDTO> scoreRules) {
        log.info("validateIntelligenceConfig intelligenceSwitch {} scoreRules {}", intelligenceSwitch, scoreRules);
        if (Boolean.TRUE.equals(intelligenceSwitch) && CollectionUtils.isEmpty(scoreRules)) {
            throw BusinessException.of(JobResponseCode.INTELLIGENCE_RULES_REQUIRED);
        }
        if (Boolean.TRUE.equals(intelligenceSwitch) && interviewType != InterviewTypeEnum.VIDEO.getCode()) {
            throw BusinessException.of(JobResponseCode.INTERVIEW_TYPE_NU_SUPPORT_INTELLIGENCE_SWITCH);
        }
        validateScoreRules(intelligenceSwitch, scoreRules);
    }

    private void validateScoreRules(Boolean intelligenceSwitch, List<IntelligenceScoreRuleDTO> scoreRules) {
        if (intelligenceSwitch == null || !intelligenceSwitch) {
            return;
        }
        if (CollectionUtils.isEmpty(scoreRules)) {
            return;
        }
        //输入内容校验
        Map<String, List<IntelligenceScoreRuleDTO>> scoreRuleInputMap = scoreRules.stream().collect(Collectors.groupingBy(IntelligenceScoreRuleDTO::getStageCode));

        //最大限制配置处理
        List<JobIntelligenceScoreRuleConfigProperties.ScoreRuleMaxLimitConfig> maxLimitConfigs = jobIntelligenceScoreRuleConfigProperties.getMaxLimitConfigs();
        maxLimitConfigs = (maxLimitConfigs == null) ? List.of() : maxLimitConfigs;
        Map<String, JobIntelligenceScoreRuleConfigProperties.ScoreRuleMaxLimitConfig> maxConfigMap = maxLimitConfigs.stream()
                .collect(Collectors.toMap(JobIntelligenceScoreRuleConfigProperties.ScoreRuleMaxLimitConfig::getStageCode,
                        Function.identity(),
                        (v1, v2) -> v2));
        scoreRuleInputMap.forEach((stagCode, values) -> {
            JobIntelligenceScoreRuleConfigProperties.ScoreRuleMaxLimitConfig scoreRuleMaxLimitConfig = maxConfigMap.get(stagCode);
            if (scoreRuleMaxLimitConfig == null || scoreRuleMaxLimitConfig.getMaxWeightSumLimit() == null) {
                return;
            }
            if (CollectionUtils.isEmpty(values)) {
                return;
            }
            int sumWeight = values.stream().map(IntelligenceScoreRuleDTO::getWeight).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
            if (sumWeight != scoreRuleMaxLimitConfig.getMaxWeightSumLimit()) {
                JobIntelligenceStageEnum stageEnum = JobIntelligenceStageEnum.getByCode(scoreRuleMaxLimitConfig.getStageCode());
                throw BusinessException.of(JobResponseCode.INTELLIGENCE_RULES_MAX_LIMIT_SUM_WEIGHT.getCode(), Optional.ofNullable(stageEnum).map(JobIntelligenceStageEnum::getDescription).orElse("") + " " + JobResponseCode.INTELLIGENCE_RULES_MAX_LIMIT_SUM_WEIGHT.getMsg());
            }
        });
    }

    private List<IntelligenceScoreRuleDTO> getReOrder(Boolean intelligenceSwitch, List<IntelligenceScoreRuleDTO> scoreRules) {
        log.info("getReOrder intelligenceSwitch {} scoreRules {}", intelligenceSwitch, scoreRules);
        if (intelligenceSwitch == null || !intelligenceSwitch) {
            return scoreRules;
        }
        if (CollectionUtils.isEmpty(scoreRules)) {
            return scoreRules;
        }
        List<JobIntelligenceScoreRuleConfigProperties.ScoreRuleConfig> configs = jobIntelligenceScoreRuleConfigProperties.getConfigs();
        Map<String, JobIntelligenceScoreRuleConfigProperties.ScoreRuleConfig> ruleMap = configs.stream()
                .collect(Collectors.toMap(JobIntelligenceScoreRuleConfigProperties.ScoreRuleConfig::getSubStageCode,
                        Function.identity(),
                        (v1, v2) -> v2));

        scoreRules.forEach(config -> {
            JobIntelligenceScoreRuleConfigProperties.ScoreRuleConfig scoreRuleConfig = ruleMap.get(config.getSubStageCode());
            config.setStageOrder(Optional.ofNullable(scoreRuleConfig).map(JobIntelligenceScoreRuleConfigProperties.ScoreRuleConfig::getStageOrder).orElse(Integer.MAX_VALUE));
            config.setSubStageOrder(Optional.ofNullable(scoreRuleConfig).map(JobIntelligenceScoreRuleConfigProperties.ScoreRuleConfig::getSubStageOrder).orElse(Integer.MAX_VALUE));
        });
        return scoreRules.stream().sorted(Comparator.comparing(IntelligenceScoreRuleDTO::getStageOrder).thenComparing(IntelligenceScoreRuleDTO::getSubStageOrder)).collect(Collectors.toList());
    }
}
