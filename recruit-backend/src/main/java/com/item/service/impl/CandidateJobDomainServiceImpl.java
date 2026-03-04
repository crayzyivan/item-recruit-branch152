package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.convert.AiVettedResultSkillConverter;
import com.item.convert.CandidateConverter;
import com.item.convert.CandidateEducationConverter;
import com.item.convert.CandidateJobConvert;
import com.item.convert.DeniedListConverter;
import com.item.convert.EmploymentHistoryConverter;
import com.item.dto.AiVettedResultDTO;
import com.item.dto.AiVettedResultSkillDTO;
import com.item.dto.AnswerQuestion5SInfoDTO;
import com.item.dto.AnswerQuestion5sTestConfirmRequestDTO;
import com.item.dto.AnswerQuestion5sTestStatusRequestDTO;
import com.item.dto.CandidateAnswerQuestionInfoDTO;
import com.item.dto.CandidateJobCountDTO;
import com.item.dto.CandidateShareVO;
import com.item.dto.CityDTO;
import com.item.dto.CompanyInfoSimpleDTO;
import com.item.dto.CountryDTO;
import com.item.dto.DictionaryDTO;
import com.item.dto.ReadyPassDto;
import com.item.dto.RejectCandidateDto;
import com.item.dto.ReviewPassDto;
import com.item.dto.StateDTO;
import com.item.dto.VettedPassDto;
import com.item.dto.ai.AIPhoneInterviewRequestDTO;
import com.item.dto.ai.GenerateInterviewLinkDTO;
import com.item.dto.ai.InterviewInvitationMailDTO;
import com.item.dto.ai.InterviewResponseDTO;
import com.item.dto.ai.ResumeAIMatchDTO;
import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.job.CandidateJobApplyDto;
import com.item.dto.job.CandidateJobUpdateStatusDto;
import com.item.dto.job.IntelligenceScoreRuleDTO;
import com.item.entity.AiVettedResultEntity;
import com.item.entity.AiVettedResultSkillEntity;
import com.item.entity.CandidateEducationEntity;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateEsEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.CountryEntity;
import com.item.entity.EmploymentHistoryEntity;
import com.item.entity.JobEntity;
import com.item.entity.JobStatusRecordEntity;
import com.item.entity.PointsOperationLog;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.es.JobEsService;
import com.item.es.ResumeEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.AiInterviewConfig;
import com.item.framework.config.BusinessDeductionPointsConfig;
import com.item.framework.config.IamCommonConfig;
import com.item.framework.config.MinuteBasedConfig;
import com.item.framework.config.RecruitCommonNacosConfig;
import com.item.framework.constant.ApplyMethodEnum;
import com.item.framework.constant.CandidateResponseCode;
import com.item.framework.constant.CommonConstants;
import static com.item.framework.constant.CommonConstants.StrConstants.QUESTION_5S;
import com.item.framework.constant.CommonResponseCode;
import static com.item.framework.constant.CommonResponseCode.COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER;
import com.item.framework.constant.DataSourceEnum;
import com.item.framework.constant.DictTypeConstans;
import com.item.framework.constant.DictionaryEnum;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.InterviewMailStatusEnum;
import com.item.framework.constant.InterviewPhoneStatusEnum;
import com.item.framework.constant.InterviewTypeEnum;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.framework.constant.JobIntelligenceSubStageEnum;
import com.item.framework.constant.JobResponseCode;
import com.item.framework.constant.JobStatus;
import static com.item.framework.constant.JobStatus.ACTIVE;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.framework.constant.PricingModelEnum;
import com.item.framework.constant.ReapplyInvitedEnum;
import com.item.framework.constant.ResumeAiScoreWeight;
import com.item.framework.constant.SoftSkillLevelEnum;
import com.item.framework.constant.TransactionNoTypeEnum;
import com.item.framework.constant.TransactionTypeEnum;
import com.item.framework.constant.UnChangeResponseCode;
import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.mapper.CountryMapper;
import com.item.service.AIService;
import com.item.service.AiVettedResultService;
import com.item.service.AiVettedResultSkillService;
import com.item.service.CandidateJobDomainService;
import com.item.service.CandidateJobService;
import com.item.service.CandidateService;
import com.item.service.CompanyService;
import com.item.service.DictionaryService;
import com.item.service.GenerateUrlCodeService;
import com.item.service.JobDomainService;
import com.item.service.JobFlowService;
import com.item.service.JobService;
import com.item.service.JobStatusRecordService;
import com.item.service.LocationService;
import com.item.service.PointService;
import com.item.service.PointsOperationLogService;
import com.item.service.ShortIdGenerator;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.service.migration.DataMigrationMappingService;
import com.item.util.CommonUtils;
import com.item.util.JsonUtils;
import com.item.util.LambdaUtil;
import com.item.util.LanguageLocalUtils;
import com.item.util.MailUtils;
import com.item.util.Md5SignatureUtil;
import com.item.util.RedisKeyUtil;
import com.item.util.RedisSerialNumberUtils;
import com.item.util.S3Utils;
import com.item.util.UserContextUtil;
import com.item.vo.AiVettedQueryVO;
import com.item.vo.AiVettedResultSkillVO;
import com.item.vo.ApplicationQueryVO;
import com.item.vo.ApplicationVO;
import com.item.vo.CandidateDetailsVO;
import com.item.vo.CandidateJobQueryVO;
import com.item.vo.CandidateJobRecordVO;
import com.item.vo.CandidateJobVO;
import com.item.vo.CandidateProcessVO;
import com.item.vo.CandidateScoreVO;
import com.item.vo.DeniedListVO;
import com.item.vo.EducationDetailsVO;
import com.item.vo.EmploymentDetailsVO;
import com.item.vo.InterviewReportVO;
import com.item.vo.ManualReviewQueryVO;
import com.item.vo.ManualReviewVO;
import com.item.vo.PendingReviewQueryVO;
import com.item.vo.PendingReviewVO;
import com.item.vo.PointLogVO;
import com.item.vo.ProcessTimeLineVO;
import com.item.vo.ReadyListVO;
import com.item.vo.ReadyQueryVO;
import com.item.vo.ai.JobMatchResultVO;
import com.item.vo.ai.WrittenDetailVO;
import com.item.vo.ai.WrittenTestAnalysisVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应聘者职位关系领域服务实现类
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class CandidateJobDomainServiceImpl implements CandidateJobDomainService {
    private final JobEsService jobEsService;
    @Value("${repeat.apply.job.limit.day:180}")
    private int repeatApplyJobLimitDay;
    @Value("${repeat.apply.job.limit.enable:true}")
    private boolean repeatApplyJobLimitEnable;
    @Value("${company.name.replace.str:,.，。#@%$}")
    private String companyNameReplace;
    @Value("${job.publish.url}")
    private String jobPublishUrl;
    @Value("${interview.report.url.expire.seconds:86400}")
    private int reportUrlExpireSeconds;

    private int expireSeconds = 3600*24;

    private final JobService jobService;
    private final CandidateJobService candidateJobService;
    private final RedissonClient redissonClient;
    private final CandidateService candidateService;
    private final JobStatusRecordService jobStatusRecordService;
    private final ResumeEsService resumeEsService;
    private final AIService aiService;
    private final JobFlowService jobFlowService;
    private final MailUtils mailUtils;
    private final AiInterviewConfig aiInterviewConfig;
    private final AiVettedResultService aiVettedResultService;
    private final AiVettedResultSkillService aiVettedResultSkillService;
    private final LocationService locationService;
    private final DictionaryService dictionaryService;
    private final S3Utils s3Utils;
    private final ThreadPoolTaskExecutor aiTaskExecutor;
    private final PointService pointService;
    private final BusinessDeductionPointsConfig businessDeductionPointsConfig;
    private final RedisSerialNumberUtils redisSerialNumberUtils;
    private final MinuteBasedConfig minuteBasedConfig;
    private final CompanyService companyService;
    private final GenerateUrlCodeService generateUrlCodeService;
    private final ShortIdGenerator shortIdGenerator;
    private final JobDomainService jobDomainService;
    private final IamRpcAdapter iamRpcAdapter;
    private final PointsOperationLogService pointsOperationLogService;
    private final DataMigrationMappingService dataMigrationMappingService;
    private final IamCommonConfig iamCommonConfig;
    private final CountryMapper countryMapper;
    private final RecruitCommonNacosConfig recruitCommonNacosConfig;

    @Override
    public boolean applyJob(CandidateJobApplyDto dto) {
        log.info("apply job dto {}", dto);
        // 通过获取 r_candidate 数据  校验一下应聘者是否存在
        IamUserContextDTO candidateNeedLogin = UserContextUtil.getCurrentUserCandidateNeedLogin();
        if (candidateNeedLogin.getCandidateOneselfId() == null || !Objects.equals(dto.getCandidateId(),
                candidateNeedLogin.getCandidateOneselfId())) {
            log.warn("current user and param user not equals {}", dto);
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_CURRENT_PARAM_NOT_EQUALS);
        }
        CandidateEntity candidateEntity = candidateService.getById(dto.getCandidateId());
        if (candidateEntity == null) {
            log.warn("candidate not found {}", dto);
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
        }
        //校验简历上传状态 简历状态等于0表示上传失败
        if (candidateEntity.getUploadStatus() != null && candidateEntity.getUploadStatus() == 0) {
            log.warn("candidate resume upload failed {}", candidateEntity);
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_RESUME_NOT_FOUND);
        }
        //校验职位数据
        JobEntity jobEntity = jobService.getJobsByIds(dto.getJobId());
        if (jobEntity == null) {
            log.warn("job not found {}", dto);
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }
        //校验职位状态是否是active（open）
        if (JobStatus.getByCode(jobEntity.getJobStatus()) != ACTIVE) {
            log.warn("job status is not active {}", dto);
            throw BusinessException.of(JobResponseCode.JOB_STATUS_NOT_ACTIVE);
        }
        //校验职位的companyCode属性是否存在
        if (StringUtils.isBlank(jobEntity.getCompanyCode())) {
            log.warn("job company code is blank {} jobEntity {}", dto, jobEntity);
            throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
        }
        CandidateJobEntity candidateJobEntity = convert(dto, jobEntity);
        LocalDateTime dateTime = LocalDateTime.now();
        candidateJobEntity.setUpdateTime(dateTime);
        //添加职位地址名称
        addJobLocationName(candidateJobEntity);
        RLock lock = redissonClient.getLock(CommonUtils.join(dto.getCandidateId(), dto.getJobId()));
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
                //获取最新记录是否允许再申请
                CandidateJobEntity latestByCandidateIdAndJob = candidateJobService.getLatestByCandidateIdAndJobId(dto.getCandidateId(), dto.getJobId());
                if (repeatApplyJobLimitEnable && !checkAllowApply(latestByCandidateIdAndJob)) {
                    log.warn("job repeat apply job limit day latest {} limit {}", latestByCandidateIdAndJob, repeatApplyJobLimitDay);
                    throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_APPLY_REPEATEDLY);
                }
                candidateJobEntity.setInterviewMailStatus(InterviewMailStatusEnum.NOTSEND.getCode());
                candidateJobEntity.setInterviewPhoneStatus(InterviewPhoneStatusEnum.NOT_SCHEDULED.getCode());
                candidateJobService.applyJob(candidateJobEntity);
                //流转记录
                jobStatusRecordService.saveJobStatusRecord(candidateJobEntity.getId(), candidateJobEntity.getCompanyCode(), null, candidateJobEntity.getApplyStatus(), JobApplyStatusEvent.SUBMIT.toString(), dateTime);
                //异步处理、定时任务补偿
                aiTaskExecutor.execute(() -> {
                    try {
                        this.resumeAIMatch(candidateJobEntity, jobEntity.getCreateBy(), jobEntity.getCompanyCode());
                    } catch (Exception e) {
                        log.error("resumeAIMatch error:candidateJobId: {}", candidateJobEntity.getId(), e);
                    }
                });
                return true;
            }
        } catch (BusinessException bex) {
            log.warn("apply repeatedly {}", dto, bex);
            throw bex;
        } catch (Exception e) {
            log.error("apply Job fail {}", dto, e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
        log.warn("apply job failed {}", dto);
        return false;
    }

    /**
     * 添加职位地址名称
     * @param candidateJobEntity
     */
    private void addJobLocationName(CandidateJobEntity candidateJobEntity){
        if (!org.springframework.util.StringUtils.hasText(candidateJobEntity.getJobCountryName()) && candidateJobEntity.getJobCountryId()!=null){
            List<CountryDTO> countryDTOS = locationService.listByCountryIds(List.of(candidateJobEntity.getJobCountryId()));
            if (CollectionUtils.isNotEmpty(countryDTOS)){
                candidateJobEntity.setJobCountryName(countryDTOS.getFirst().getName());
            }
        }
        if (!org.springframework.util.StringUtils.hasText(candidateJobEntity.getJobStateName()) && candidateJobEntity.getJobStateId()!=null){
            List<StateDTO> stateDTOS = locationService.listByStateIds(List.of(candidateJobEntity.getJobStateId()));
            if (CollectionUtils.isNotEmpty(stateDTOS)){
                candidateJobEntity.setJobStateName(stateDTOS.getFirst().getName());
            }
        }
        if (!org.springframework.util.StringUtils.hasText(candidateJobEntity.getJobCityName()) && candidateJobEntity.getJobCityId()!=null){
            List<CityDTO> cityDTOS = locationService.listByCityIds(List.of(candidateJobEntity.getJobCityId()));
            if (CollectionUtils.isNotEmpty(cityDTOS)){
                candidateJobEntity.setJobCityName(cityDTOS.getFirst().getName());
            }
        }
    }

    @Override
    @Transactional
    public IPage<ResumeAIMatchDTO> resumeAIMatch(int pageNo, int pageSize, ResumeAIMatchDTO dto) {
        Page<CandidateJobEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new QueryWrapper<CandidateJobEntity>().lambda();
        if(Objects.nonNull(dto.getJobId()) && dto.getJobId() > 0) {
            queryWrapper.eq(CandidateJobEntity::getJobId, dto.getJobId());
        }
        queryWrapper.eq(CandidateJobEntity::getApplyStatus,JobApplyStatus.SCREENED.getCode());
        if (Objects.nonNull(dto.getScore())) {
            queryWrapper.and(wrapper -> wrapper
                    .ge(CandidateJobEntity::getAssessmentScore, dto.getScore())
                    .or()
                    .ge(CandidateJobEntity::getOverallScore, dto.getScore())
            );
        }
        queryWrapper.orderByDesc(CandidateJobEntity::getCreateTime);
        candidateJobService.page(page, queryWrapper);
        List<ResumeAIMatchDTO> dtoList = CandidateJobConvert.INSTANCE.tolistDto(page.getRecords());

        if (CollectionUtils.isNotEmpty(dtoList)) {
            // 收集所有候选人ID
            List<Long> candidateIds = dtoList.stream()
                    .map(ResumeAIMatchDTO::getCandidateId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(candidateIds)) {
                // 批量查询候选人信息
                List<CandidateEntity> candidateEntities = candidateService.getByIds(candidateIds);
                Map<Long, CandidateEntity> candidateMap = candidateEntities.stream()
                        .collect(Collectors.toMap(CandidateEntity::getId, Function.identity()));

                // 使用Map进行数据填充，避免循环查询
                dtoList.forEach(resumeAIMatchDTO -> {
                    CandidateEntity candidateEntity = candidateMap.get(resumeAIMatchDTO.getCandidateId());
                    if (candidateEntity != null) {
                        resumeAIMatchDTO.setCandidateId(candidateEntity.getId());
                        resumeAIMatchDTO.setCandidateEmail(candidateEntity.getCandidateEmail());
                        resumeAIMatchDTO.setCandidateName(candidateEntity.getCandidateName());
                    }
                });
            }
        }
        IPage<ResumeAIMatchDTO> result = new Page<>(pageNo, pageSize,page.getTotal());
        result.setRecords(dtoList);

        return result;
    }

    @Override
    public String createAiInterview(Long candidateJobId) {
        CandidateJobEntity candidateJobEntity = candidateJobService.getById(candidateJobId);
        RLock lock = redissonClient.getLock(RedisKeyUtil.getLockSendInterviewUrl(candidateJobId));
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
                //冻结记录
                PointsOperationLog pointsOperationLog = pointsOperationLogService.selectinterviewFreezeLog(candidateJobEntity.getId());
                if (pointsOperationLog==null) {
                    IamUserContextDTO currentUserRecruitNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
                    processInterviewMail(candidateJobEntity,Long.valueOf(currentUserRecruitNeedLogin.getId()),false);
                } else {
                    processAiInterview(candidateJobEntity, false);
                }
            } else {
                throw new BusinessException(GlobalStatusCode.SENDING_EMAIL,"Sending interview email...");
            }
        } catch (Exception e) {
            log.error("interviewMail error:candidateJobId: {}", candidateJobId, e);
            throw e;
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
        return "success";
    }

    @Override
    public CandidateJobCountDTO getCandidateNumber(Long jobId) {
        return candidateJobService.getCandidateNumber(jobId);
    }

    
    /**
     * 发送面试邮件
     * @param candidateJobEntity 候选人职位关联实体
     * @param isAutoTrigger 是否为自动触发（true:自动触发，根据岗位地理位置选择模板；false:手动触发，根据用户语言环境选择模板）
     * @return 候选人职位关联实体
     */
    public CandidateJobEntity sendMail(JobEntity jobEntity, JobEsEntity jobEsEntity, CandidateJobEntity candidateJobEntity, boolean isAutoTrigger){
        if (Objects.nonNull(jobEntity) && StringUtils.isNotEmpty(jobEntity.getInterviewUrlId())){
            LambdaQueryWrapper<CandidateEntity> candidateQueryWrapper = new QueryWrapper<CandidateEntity>().lambda();
            candidateQueryWrapper.eq(CandidateEntity::getId, candidateJobEntity.getCandidateId());
            CandidateEntity candidateEntity = candidateService.getOne(candidateQueryWrapper);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            LocalDateTime interviewStartTime = LocalDateTime.now();
            LocalDateTime interviewEndTime = interviewStartTime.plusDays(aiInterviewConfig.getInterviewTimeLimit());
            GenerateInterviewLinkDTO generateInterviewLinkDTO = new GenerateInterviewLinkDTO();
            generateInterviewLinkDTO.setInterviewid(jobEntity.getInterviewUrlId());
            generateInterviewLinkDTO.setCandidateEmail(candidateEntity.getCandidateEmail());
            generateInterviewLinkDTO.setCandidateName(candidateEntity.getCandidateName());
            generateInterviewLinkDTO.setApplicationId(candidateJobEntity.getId());
            generateInterviewLinkDTO.setScheduledTime(interviewStartTime);
            CountryEntity countryEntity = countryMapper.selectById(candidateJobEntity.getJobCountryId());
            if (Objects.nonNull(countryEntity)){
                generateInterviewLinkDTO.setLocation(countryEntity.getName());
            }
            if (StringUtils.isEmpty(candidateJobEntity.getInterviewUrl())) {

                log.info("sendMail 关联id:{}",candidateJobEntity.getId());
                long start=System.currentTimeMillis();
                InterviewResponseDTO interviewResponseDTO = aiService.getInterviewLink(generateInterviewLinkDTO,jobEntity);
                if (interviewResponseDTO != null) {
                    String interviewUrl = interviewResponseDTO.getInterviewLink();
                    candidateJobEntity.setInterviewUrl(interviewUrl);
                } else {
                    log.error("Failed to get interview URL");
                    throw new BusinessException(GlobalStatusCode.EMAIL_SEND_ERROR,"Failed to get interview URL.");
                }
                log.info("sendMail 关联id:{},耗时:{},结果:{}",candidateJobEntity.getId(),System.currentTimeMillis() - start,interviewResponseDTO);
            }
            IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(jobEntity.getCompanyCode());
            
            // 判断是否需要显示账号密码信息
            // 条件：申请方式为邀请面试自动投递 且 密码未告知（isPasswordTold为null或0）
            boolean showAccountInfo = false;
            String account = null;
            String password = null;
            boolean isInviteInterviewApply = Objects.equals(candidateJobEntity.getApplyMethod(), ApplyMethodEnum.INVITE_INTERVIEW_AUTO_APPLY.getCode());
            if (isInviteInterviewApply) {
                Integer isPasswordTold = candidateEntity.getIsPasswordTold();
                if (isPasswordTold !=null && isPasswordTold == 0) {
                    showAccountInfo = true;
                    // 账号使用邮箱
                    account = StringUtils.isNotEmpty(candidateEntity.getCandidatePermanentEmail())
                            ? candidateEntity.getCandidatePermanentEmail() 
                            : candidateEntity.getCandidateEmail();
                    // 密码优先使用候选人表中的密码，如果为空则使用配置的默认密码
                    password = StringUtils.isNotEmpty(candidateEntity.getPassword())
                            ? candidateEntity.getPassword() 
                            : iamCommonConfig.getRegisterCandidate().getDefaultPassword();
                }
            }

            String platformUrl = null;
            // 获取平台地址（邀请面试渠道投递时使用）
            if (isInviteInterviewApply && StringUtils.isNotEmpty(jobPublishUrl)) {
                platformUrl = jobPublishUrl;
            }
            
            InterviewInvitationMailDTO.InterviewInvitationMailDTOBuilder builder = InterviewInvitationMailDTO.builder()
                    .jobTitle(jobEntity.getTitle())
                    .companyName(companyInfo.getCompanyName())
                    .candidateName(candidateEntity.getCandidateName())
                    .interviewUrl(aiInterviewConfig.getInterviewUrl()+candidateJobEntity.getInterviewUrl())
                    .startDate(interviewStartTime.format(formatter))
                    .endDate(interviewEndTime.format(formatter))
                    .interviewTimeLimit(aiInterviewConfig.getInterviewTimeLimit())
                    .time(String.valueOf(jobEntity.getInterviewLength() == null?aiInterviewConfig.getTimeDuration():jobEntity.getInterviewLength()))
                    .showAccountInfo(showAccountInfo);
            
            if (showAccountInfo) {
                builder.account(account).password(password);
            }
            if (StringUtils.isNotEmpty(platformUrl)) {
                builder.platformUrl(platformUrl);
            }
            
            InterviewInvitationMailDTO interviewInvitationMailDTO = builder.build();
            ObjectMapper mapper = new ObjectMapper();
            
            // 根据是否为自动触发选择模板和主题
            boolean chinese;
            String templateName;
            String subject;

            // 自动触发：根据岗位地理位置选择模板
            chinese = isJobLocationInChina(candidateJobEntity);

            Map<String, Object> templateParam = mapper.convertValue(interviewInvitationMailDTO, new TypeReference<Map<String, Object>>() {});
            if (jobEsEntity != null && jobEsEntity.getEnableQuestion5STest() != null && jobEsEntity.getEnableQuestion5STest()) {
                templateName = chinese ? "Interview-invitation-video-zh.html" : "Interview-invitation-video-en.html";
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                String nowStr = now.toEpochSecond(ZoneOffset.UTC) + "";
                String link = generateUrlCodeService.generateUrlQuestionLink(candidateJobEntity.getId(), nowStr);
                templateParam.put("writtenTestUrl", link);
            } else {
                templateName = chinese ? "Interview-invitation-zh.html" : "Interview-invitation.html";
            }
            subject = chinese
                    ? String.format("AI面试邀请 - %s 在 %s", interviewInvitationMailDTO.getJobTitle(), interviewInvitationMailDTO.getCompanyName())
                    : String.format("AI Interview Invitation - %s at %s", interviewInvitationMailDTO.getJobTitle(), interviewInvitationMailDTO.getCompanyName());
            log.info("Auto trigger email: candidateJobId={}, jobCountryId={}, chinese={}",
                    candidateJobEntity.getId(), candidateJobEntity.getJobCountryId(), chinese);
            mailUtils.sendHtmlTemplateMail("no-reply@item.com",candidateEntity.getCandidateEmail(),
                    subject,
                    templateName,templateParam);
            candidateJobEntity.setInterviewMailStatus(InterviewMailStatusEnum.SEND.getCode());
            candidateJobEntity.setInterviewStartTime(interviewStartTime);
            candidateJobEntity.setInterviewEndTime(interviewEndTime);
            candidateJobService.updateById(candidateJobEntity);
            
            // 如果显示了账号密码信息，更新isPasswordTold为1（已告知）
            if (showAccountInfo) {
                candidateEntity.setIsPasswordTold(1);
                candidateService.updateById(candidateEntity);
                log.info("Updated isPasswordTold to 1 for candidateId: {}", candidateEntity.getId());
            }
        } else {
            log.error("interviewMail task Failed to send interview mail for candidateJobId:{},The position does not exist or has been deleted.", candidateJobEntity.getJobId());
        }
        return candidateJobEntity;
    }

    public CandidateJobEntity processAiInterview(CandidateJobEntity candidateJob, boolean isAutoTrigger) {
        JobEntity job = jobService.getById(candidateJob.getJobId());
        if (job == null || StringUtils.isAllEmpty(job.getInterviewUrlId())) {
            log.error("Create AI phone call failed for jobId:{}, Because this job not create interview id.", candidateJob.getJobId());
            return candidateJob;
        }
        JobEsEntity jobEsEntity = jobEsService.getJobById(job.getId(), LambdaUtil.getFieldNames(JobEsEntity::getEnableQuestion5STest));
        if (InterviewTypeEnum.AI_PHONE.getCode() == job.getInterviewType()) {
            return createAiPhoneCall(job, jobEsEntity, candidateJob);
        }
        return sendMail(job, jobEsEntity, candidateJob, isAutoTrigger);
    }

    /**
     * 创建 AI 电话面试，调用 AI 服务，由 AI 服务定时拨打电话进行电话面试
     * @param candidateJob  候选人及职位信息
     */
    public CandidateJobEntity createAiPhoneCall(JobEntity job, JobEsEntity jobEsEntity, CandidateJobEntity candidateJob) {
        log.info("Call bookingAiPhoneInterview candidateJob: {}", JsonUtils.toJson(candidateJob));

        CandidateJobEntity entity = candidateJobService.getById(candidateJob.getId());
        if (entity != null && Objects.equals(InterviewPhoneStatusEnum.SCHEDULED.getCode(), entity.getInterviewPhoneStatus())) {
            throw new BusinessException(GlobalStatusCode.AI_PHONE_CALL_FAILED, "A phone interview has been scheduled.");
        }

        String interviewTime = getInterviewTime(candidateJob);
        CandidateEntity candidate = candidateService.getById(candidateJob.getCandidateId());
        AIPhoneInterviewRequestDTO requestDTO = new AIPhoneInterviewRequestDTO();
        requestDTO.setInterviewId(job.getInterviewUrlId());
        requestDTO.setApplicationId(candidateJob.getId().toString());
        requestDTO.setCandidateName(candidate.getCandidateName());
        requestDTO.setCandidateEmail(candidate.getCandidateEmail());
        requestDTO.setCandidatePhone(candidateJob.getInterviewPhone());
        requestDTO.setScheduledTime(interviewTime);
        requestDTO.setLanguage(candidateJob.getInterviewLanguage());
        boolean success = aiService.createAIPhoneInterview(requestDTO, job);
        if (!success) {
            log.error("Call bookingAiPhoneInterview result: {}", success);
            throw new BusinessException(GlobalStatusCode.AI_PHONE_CALL_FAILED, "CandidateJob Id:" + candidateJob.getId().toString());
        }

        if (jobEsEntity != null && jobEsEntity.getEnableQuestion5STest() != null && jobEsEntity.getEnableQuestion5STest()) {
            sendWritten5STestInvitationEmail(job, candidateJob, candidate);
        }

        candidateJob.setInterviewPhoneStatus(InterviewPhoneStatusEnum.SCHEDULED.getCode());
        candidateJobService.updateById(candidateJob);

        return candidateJob;
    }

    private void sendWritten5STestInvitationEmail(JobEntity job, CandidateJobEntity candidateJob, CandidateEntity candidate) {
        boolean jobLocationInChina = isJobLocationInChina(candidateJob);
        String template = jobLocationInChina ? "Interview-invitation-phone-zh.html" : "Interview-invitation-phone-en.html";
        IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(job.getCompanyCode());
        String companyName = companyInfo.getCompanyName();
        String candidateName = candidate.getCandidateName();
        String jobTitle = job.getTitle();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String nowStr = now.toEpochSecond(ZoneOffset.UTC) + "";
        String writtenTestUrl = generateUrlCodeService.generateUrlQuestionLink(candidateJob.getId(), nowStr);
        String subject = jobLocationInChina
                ? String.format("笔试邀请 - %s 在 %s", jobTitle, companyName)
                : String.format("Written test invitation - %s at %s", jobTitle, companyName);
        log.info("sendWritten5STestInvitationEmail email: template {}, companyName {}, chinese {} candidateName {} jobTitle {} nowStr {} writtenTestUrl {} subject {}",
                template, companyName, jobLocationInChina, candidateName, jobTitle, nowStr, writtenTestUrl, subject);
        Map<String, Object> templateParam = Map.of("companyName", companyName,
                "candidateName", candidateName,
                "jobTitle", jobTitle,
                "writtenTestUrl", writtenTestUrl);
        mailUtils.sendHtmlTemplateMail(candidate.getCandidateEmail(), subject, template, templateParam);
    }

    /**
     *  ai简历筛选 更新招聘状态
     * @param candidateJobEntity
     * @return
     */
    @Override
    public ResumeAIMatchDTO resumeAIMatch(CandidateJobEntity candidateJobEntity,Long userId,String companyCode) {
        ResumeAIMatchDTO resumeAIMatchDTO = CandidateJobConvert.INSTANCE.toEntitybDTO(candidateJobEntity);
        int assessmentScore=0;
        //智能匹配开关
        boolean autoFlag=false;
        //智能匹配 自动通过分数
        Integer autoPassScreenScore=null;
        if (resumeAIMatchDTO.getAssessmentScore() == null) {
            RLock lock = redissonClient.getLock("resumeAIMatch:"+candidateJobEntity.getId());
            boolean locked = false;
            try{
                locked = lock.tryLock();
                if (locked) {
                    //积分扣除
                    resumeScreenPointHandle(userId,companyCode,candidateJobEntity);
                    //ai筛选接口和评分规则
                    log.info("ai简历筛选:关联id:{}",candidateJobEntity.getId());
                    long start=System.currentTimeMillis();
                    JobMatchResultVO jobMatchResultVO = aiService.aiMatch(candidateJobEntity);
                    log.info("ai简历筛选:关联id:{},耗时：{}",candidateJobEntity.getId(),System.currentTimeMillis()-start);
                    assessmentScore = calculateTotalScoreWithWeight(jobMatchResultVO);
                    jobMatchResultVO.setPlaceId(candidateJobEntity.getPlaceId());
                    candidateJobEntity.setAssessmentScore(assessmentScore);
                    resumeAIMatchDTO.setAssessmentScore(assessmentScore);
                    resumeAIMatchDTO.setRecommendation(candidateJobEntity.getRecommendation());
                    CandidateJobConvert.INSTANCE.dtoToEntity(resumeAIMatchDTO, jobMatchResultVO);
                    jobMatchResultVO.setCreateTime(LocalDateTime.now());
                    jobMatchResultVO.setUpdateTime(LocalDateTime.now());
                    jobMatchResultVO.setDeleted(0);
                    jobMatchResultVO.setApplyStatus(candidateJobEntity.getApplyStatus());
                    Map<Long, String> degreeMap = dictionaryService
                            .listByTypes(List.of(DictTypeConstans.SALARYTYPE,DictTypeConstans.CURRENCYTYPE))
                            .stream()
                            .collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue));
                    CandidateEntity candidateEntity = candidateService.getById(candidateJobEntity.getCandidateId());
                    if (Objects.nonNull(candidateEntity)) {
                        jobMatchResultVO.setCurrencyName(Objects.nonNull(candidateEntity.getCurrencyTypeId())?degreeMap.get(candidateEntity.getCurrencyTypeId()):"");

                    }
                    CandidateJobConvert.INSTANCE.candidateEntityToJobMatchResultVO(candidateEntity, jobMatchResultVO);
                    JobEntity jobEntity = jobService.getById(candidateJobEntity.getJobId());
                    CandidateJobConvert.INSTANCE.jobEntityToJobMatchResultVO(jobEntity, jobMatchResultVO);
                    //字典查询
                    if (Objects.nonNull(jobEntity)) {
                        CandidateJobConvert.INSTANCE.jobEntityToJobMatchResultVO(jobEntity, jobMatchResultVO);
                        jobMatchResultVO.setJobCurrencyName(Objects.nonNull(jobEntity.getCurrency())?degreeMap.get(Long.valueOf(jobEntity.getCurrency())):"");
                        jobMatchResultVO.setSalaryTypeName(Objects.nonNull(jobEntity.getSalaryType())?degreeMap.get(Long.valueOf(jobEntity.getSalaryType())):"");
                    }
                    //job多地址处理
                    if (candidateJobEntity.getJobCityId()!=null){
                        jobMatchResultVO.setLocationId(candidateJobEntity.getJobCityId().intValue());
                        jobMatchResultVO.setLocationName(CommonUtils.getLocationName(candidateJobEntity.getJobCountryName(),candidateJobEntity.getJobStateName(),candidateJobEntity.getJobCityName()));
                    }
                    jobMatchResultVO.setApplyStatusName(JobApplyStatus.fromCode(candidateJobEntity.getApplyStatus()).getName());
                    //智能评估处理
                    List<JobEsEntity> jobEsEntities = jobEsService.listJobIntelligenceScoreRuleByIds(Collections.singletonList(candidateJobEntity.getJobId()));
                    if (CollectionUtils.isNotEmpty(jobEsEntities)){
                        JobEsEntity jobEsEntity = jobEsEntities.getFirst();
                        if (jobEsEntity.getIntelligenceSwitch()!=null){
                            autoFlag=jobEsEntity.getIntelligenceSwitch();
                            if (autoFlag){
                                IntelligenceScoreRuleDTO intelligenceScoreRuleDTO = getIntelligenceScoreRuleDTO(jobEsEntity.getScoreRules(), JobIntelligenceSubStageEnum.ASSESSMENT_SCORE.getCode());
                                if (intelligenceScoreRuleDTO!=null){
                                    autoPassScreenScore=intelligenceScoreRuleDTO.getThreshold();
                                    jobMatchResultVO.setScoreRules(List.of(intelligenceScoreRuleDTO));
                                }
                            }
                        }
                    }
                    //推荐意见
                    candidateJobEntity.setRecommendation(getSuggestion(assessmentScore,autoFlag,autoPassScreenScore));
                    jobMatchResultVO.setRecommendation(candidateJobEntity.getRecommendation());
                    //更新表评分
                    candidateJobService.updateById(candidateJobEntity);
                    resumeEsService.saveResumeAiResultToEs(jobMatchResultVO);
                    jobFlowService.fireEvent(candidateJobEntity.getId(),JobApplyStatusEvent.SCREEN);
                    candidateJobEntity.setApplyStatus(JobApplyStatus.SCREENED.getCode());
                }else {
                    throw BusinessException.of(COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER);
                }
            }catch (Exception e){
                log.error("resumeAIMatch task Failed to save resumeAIMatch for candidateJobId:{}", candidateJobEntity.getId(), e);
            }finally {
                if (locked) {
                    lock.unlock();
                }
            }

            //智能匹配处理
            //菲律宾同步数据不自动发送邮件
            log.info("ai简历筛选:关联id:{},assessmentScore:{},autoFlag:{},autoPassScreenScore:{}",candidateJobEntity.getId(),assessmentScore,autoFlag,autoPassScreenScore);
            Optional<DataMigrationMappingEntity> mappingEntityOptional = dataMigrationMappingService.findByMysqlIdAndType(candidateJobEntity.getId(), MigrationBusTypeEnum.CANDIDATE_JOB);
            if (autoFlag){
                //分数通过自动发邮件、分数不通过拒绝
                if (autoPassScreenScore!=null && assessmentScore >= autoPassScreenScore && candidateJobEntity.getInterviewMailStatus().equals(InterviewMailStatusEnum.NOTSEND.getCode()) && mappingEntityOptional.isEmpty()) {
                    processInterviewMail(candidateJobEntity,userId,true);
                }else{
                    jobFlowService.fireEvent(candidateJobEntity.getId(),JobApplyStatusEvent.REJECT);
                }
            }else{
                if (assessmentScore >= CommonConstants.MIN_INTERVIEW_MAIL_SCORE && candidateJobEntity.getInterviewMailStatus().equals(InterviewMailStatusEnum.NOTSEND.getCode()) && mappingEntityOptional.isEmpty()) {
                    processInterviewMail(candidateJobEntity,userId,true);
                }
            }
        }
        return resumeAIMatchDTO;
    }

    /**
     * 获取智能评估规则
     * @param scoreRules
     * @param subStageCode
     * @return
     */
    private IntelligenceScoreRuleDTO getIntelligenceScoreRuleDTO(List<IntelligenceScoreRuleDTO> scoreRules,String subStageCode){
        if (CollectionUtils.isNotEmpty(scoreRules)){
            Optional<IntelligenceScoreRuleDTO> optional = scoreRules.stream().filter(c -> Objects.equals(c.getSubStageCode(), subStageCode)).findFirst();
            if (optional.isPresent()){
                return optional.get();
            }
        }
        return null;
    }

    /**
     * ai筛选积分扣除
     */
    private void resumeScreenPointHandle(Long operUserId,String companyCode,CandidateJobEntity candidateJobEntity){
        //是否豁免积分
        boolean isExempt=pointService.isExempt(companyCode);
        if (!isExempt){
            //查询是否已经扣除积分
            PointsOperationLog pointsOperationLog = pointsOperationLogService.selectScreenPointsOperationLog(candidateJobEntity.getId());
            if (pointsOperationLog==null){
                PointLogVO pointLogVO=new PointLogVO();
                pointLogVO.setCandidateJobId(candidateJobEntity.getId());
                pointLogVO.setCandidateId(candidateJobEntity.getCandidateId());
                pointLogVO.setJobId(candidateJobEntity.getJobId());
                pointLogVO.setCompanyCode(companyCode);
                pointLogVO.setPoints(businessDeductionPointsConfig.getResumeScreen().getDeductedPoints());
                pointLogVO.setTransactionNo(redisSerialNumberUtils.generate(TransactionNoTypeEnum.RESUME_SCREEN.getCode()));
                pointLogVO.setTransactionType(TransactionTypeEnum.RESUME_SCREEN.getCode());
                Long userPrimaryId = pointService.getUserPrimaryId(companyCode);
                if (userPrimaryId==null){
                    throw new BusinessException(UnChangeResponseCode.PRIMARY_ACCOUNT_NOT_FOUND);
                }
                pointLogVO.setUserId(userPrimaryId);
                pointLogVO.setOperUserId(operUserId);
                if (!pointService.deductCreditCenterPoints(pointLogVO)){
                    throw new BusinessException(UnChangeResponseCode.INSUFFICIENT_POINTS);
                }
            }
        }
    }

    public static int calculateTotalScoreWithWeight(JobMatchResultVO result) {
        int total = 0;
        int sumWeight = 0;

        total += safeScore(result.getJobTitleScore()) * ResumeAiScoreWeight.JOB_TITLE;
        sumWeight += ResumeAiScoreWeight.JOB_TITLE;

        total += safeScore(result.getSkillsScore()) * ResumeAiScoreWeight.SKILLS;
        sumWeight += ResumeAiScoreWeight.SKILLS;

        total += safeScore(result.getRequirementScore()) * ResumeAiScoreWeight.REQUIREMENT;
        sumWeight += ResumeAiScoreWeight.REQUIREMENT;

        total += safeScore(result.getResponsibilityScore()) * ResumeAiScoreWeight.RESPONSIBILITY;
        sumWeight += ResumeAiScoreWeight.RESPONSIBILITY;

        total += safeScore(result.getExperienceScore()) * ResumeAiScoreWeight.EXPERIENCE;
        sumWeight += ResumeAiScoreWeight.EXPERIENCE;

        total += safeScore(result.getLocationScore()) * ResumeAiScoreWeight.LOCATION;
        sumWeight += ResumeAiScoreWeight.LOCATION;

        total += safeScore(result.getMinimumSalaryScore()) * ResumeAiScoreWeight.MINIMUM_SALARY;
        sumWeight += ResumeAiScoreWeight.MINIMUM_SALARY;

        total += safeScore(result.getMaximumSalaryScore()) * ResumeAiScoreWeight.MAXIMUM_SALARY;
        sumWeight += ResumeAiScoreWeight.MAXIMUM_SALARY;

        // 由于每项分数是百分制，需除以100再乘以权重
        return Math.round(total * 1.0f / sumWeight);
    }

    private static int safeScore(Integer score) {
        return score == null ? 0 : score;
    }

    /**
     * 根据总分给出建议
     * @param totalScore 总分
     * @return 建议
     */
    private Long getSuggestion(int totalScore,Boolean autoFlag,Integer autoPassScreenScore) {
        if (autoFlag){
            if (totalScore >= autoPassScreenScore) {
                return 1l;
            } else {
                return 2l;
            }
        }else{
            if (totalScore >= aiInterviewConfig.getProceedWithApplicationScore()) {
                return 1l;
            } else {
                return 2l;
            }
        }
    }

    @Deprecated
    @Override
    public boolean updateStatus(CandidateJobUpdateStatusDto dto) {
        // 1. 验证记录是否存在
        CandidateJobEntity entity = candidateJobService.getById(dto.getId());
        if (entity == null) {
            throw BusinessException.of(1001, "Record not found");
        }

        // 2. 验证状态是否有效
        JobApplyStatus status = JobApplyStatus.fromCode(dto.getStatus());
        if (status == null) {
            throw BusinessException.of(2001, "Invalid status");
        }

        // 3. 更新状态和原因
        return candidateJobService.updateApplyStatus(dto.getId(), dto.getStatus(), dto.getReason());
    }

    private static CandidateJobEntity convert(CandidateJobApplyDto dto, JobEntity jobEntity) {
        CandidateJobEntity candidateJobEntity = new CandidateJobEntity();
        candidateJobEntity.setCandidateId(dto.getCandidateId());
        candidateJobEntity.setCompanyCode(jobEntity.getCompanyCode());
        candidateJobEntity.setJobId(dto.getJobId());
        candidateJobEntity.setCustomerId(jobEntity.getCustomerId());
        candidateJobEntity.setCoverLetter(dto.getCoverLetter());
        candidateJobEntity.setPosted(0);
        candidateJobEntity.setApplyStatus(JobApplyStatus.SUBMITTED.getCode());
        candidateJobEntity.setReason("");
        candidateJobEntity.setJobCountryId(dto.getJobCountryId());
        candidateJobEntity.setJobStateId(dto.getJobStateId());
        candidateJobEntity.setJobCityId(dto.getJobCityId());
        candidateJobEntity.setJobCountryName(dto.getJobCountryName());
        candidateJobEntity.setJobStateName(dto.getJobStateName());
        candidateJobEntity.setJobCityName(dto.getJobCityName());
        candidateJobEntity.setPlaceId(dto.getPlaceId());
        if (dto.getPreferredInterviewStartTime() != null) {
            candidateJobEntity.setPreferredInterviewStartTime(dto.getPreferredInterviewStartTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        if (dto.getPreferredInterviewEndTime() != null) {
            candidateJobEntity.setPreferredInterviewEndTime(dto.getPreferredInterviewEndTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        candidateJobEntity.setInterviewPhone(dto.getInterviewPhone());
        candidateJobEntity.setInterviewLanguage(dto.getInterviewLanguage());
        return candidateJobEntity;
    }

    private boolean checkAllowApply(CandidateJobEntity candidateJobEntity) {
        if (candidateJobEntity == null) {
            return true;
        }

        // 检查是否被邀请重新申请，如果是则允许申请（绕过冷却期）
        if (ReapplyInvitedEnum.isInvited(candidateJobEntity.getReapplyInvited())) {
            log.info("Candidate has been invited to reapply, bypassing cooldown period for candidateJobId: {}",
                candidateJobEntity.getId());
            return true;
        }

        // 按现有冷却期逻辑检查
        LocalDateTime createTime = candidateJobEntity.getCreateTime();
        LocalDateTime now = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(createTime, now);
        return daysBetween > repeatApplyJobLimitDay;
    }


    /**
     * 获取AI筛选结果列表
     *
     * @param vo 筛选结果查询参数
     * @return 筛选结果列表
     */
    @Override
    public IPage<AiVettedResultDTO> selectAiVettedPageList(AiVettedQueryVO vo) {
        CandidateJobQueryVO queryVO=CandidateJobQueryVO.builder().pageIndex(vo.getPageIndex()).pageSize(vo.getPageSize())
                .jobId(vo.getJobId()).applyStatus(JobApplyStatus.VETTED.getCode()).score(vo.getScore()).build();
        //1 查询符合要求的候选人与职位表记录
        Page<CandidateJobEntity> page=candidateJobService.selectCandidateJobPageList(queryVO);
        //转换后的ai面试结果列表
        List<AiVettedResultDTO> aiVettedDTOS=new ArrayList<>();
        List<CandidateJobEntity> records = page.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            //候选人与职位关联表id
            List<Long> candidateJobIds = records.stream().map(CandidateJobEntity::getId).collect(Collectors.toList());
            //候选人id
            List<Long> candidateIds = records.stream().map(CandidateJobEntity::getCandidateId).collect(Collectors.toList());
            //候选人信息
            List<CandidateEntity> candidateList = candidateService.listByIds(candidateIds);
            //候选人ai面试结果信息
            List<AiVettedResultEntity> aiVettedResultList = aiVettedResultService.listByCandidateJobIds(candidateJobIds);
            List<AiVettedResultSkillEntity> resultSkillList=new ArrayList<>();
            if (CollectionUtils.isNotEmpty(aiVettedResultList)){
                //候选人ai面试结果表id
                List<Long> aiVettedIds = aiVettedResultList.stream().map(AiVettedResultEntity::getId).collect(Collectors.toList());
                //ai面试结果技能明细信息
                resultSkillList = aiVettedResultSkillService.listByVettedResultIds(aiVettedIds);
            }
            //自动开关
            Boolean autoFlag=getAutoFlag(vo.getJobId());
            //转换成ai面试结果列表
            aiVettedDTOS=convertAiVettedDTO(records,candidateList,aiVettedResultList, resultSkillList,autoFlag);
        }

        IPage<AiVettedResultDTO> result = new Page<>(vo.getPageIndex(), vo.getPageSize(),page.getTotal());
        result.setRecords(aiVettedDTOS);
        return result;
    }

    /**
     * 转换成ai面试结果列表
     * @param records 符合要求的成候选人与职位关联表记录
     * @param candidateList 候选人信息
     * @param aiVettedResultList ai面试审批结果
     * @param resultSkillList ai面试审批结果技能明细信息
     * @return ai面试结果列表信息
     */
    private List<AiVettedResultDTO> convertAiVettedDTO(List<CandidateJobEntity> records, List<CandidateEntity> candidateList,
                                                      List<AiVettedResultEntity> aiVettedResultList, List<AiVettedResultSkillEntity> resultSkillList,
                                                       Boolean autoFlag){
        //ai面试结果列表信息
        List<AiVettedResultDTO> dtoList=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)){
            for (CandidateJobEntity candidateJob: records){
                AiVettedResultDTO dto=new AiVettedResultDTO();
                dto.setId(candidateJob.getId());
                dto.setCandidateId(candidateJob.getCandidateId());
                //候选人信息
                Optional<CandidateEntity> candidateOptional = candidateList.stream().filter(c -> candidateJob.getCandidateId() != null && candidateJob.getCandidateId().equals(c.getId())).findFirst();
                if (candidateOptional.isPresent()){
                    dto.setCandidateName(candidateOptional.get().getCandidateName());
                    dto.setCandidateEmail(candidateOptional.get().getCandidateEmail());
                }
                //ai面试结果
                Optional<AiVettedResultEntity> aiVettedResultOptional = aiVettedResultList.stream().filter(c -> candidateJob.getId().equals(c.getCandidateJobId())).findFirst();
                if (aiVettedResultOptional.isPresent()){
                    dto.setReportId(aiVettedResultOptional.get().getId());
                    dto.setInterviewTime(aiVettedResultOptional.get().getInterviewTime());
                    dto.setInterviewScore(aiVettedResultOptional.get().getInterviewScore());
                    dto.setProctoringScore(aiVettedResultOptional.get().getProctoringScore());
                    dto.setInterviewType(aiVettedResultOptional.get().getInterviewType());
                    //ai面试结果技能明细信息
                    List<AiVettedResultSkillEntity> skilllist=resultSkillList.stream().filter(c->dto.getReportId().equals(c.getVettedResultId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(skilllist)){
                        // 过滤掉软技能，保留其他技能
                        skilllist = skilllist.stream()
                                .filter(skill -> !StringUtils.equals(skill.getSkillName(), CommonConstants.AI_VETTED_SOFT_SKILL_NAME))
                                .collect(Collectors.toList());
                    }
                    List<AiVettedResultSkillDTO> skillDTOList= AiVettedResultSkillConverter.INSTANCE.toDtoList(skilllist);
                    dto.setSkillList(skillDTOList);
                }

                //候选人各项评分
                CandidateScoreVO candidateScoreVO = convertCandidateScoreVO(candidateJob, aiVettedResultList,autoFlag);
                dto.setCandidateScore(candidateScoreVO);
                dtoList.add(dto);
            }
        }
        return dtoList;
    }


    /**
     * 获取待人工审核列表
     * @param vo
     * @return
     */
    @Override
    public Pager<PendingReviewVO> selectPendingReviewPageList(PendingReviewQueryVO vo) {
        CandidateJobQueryVO queryVO= CandidateJobQueryVO.builder().pageIndex(vo.getPageIndex()).pageSize(vo.getPageSize())
                .jobId(vo.getJobId()).applyStatus(JobApplyStatus.REVIEW.getCode()).score(vo.getScore()).build();
        //查询符合要求的候选人与职位表记录
        Page<CandidateJobEntity> page=candidateJobService.selectCandidateJobPageList(queryVO);
        //人工审核列表数据
        List<PendingReviewVO> pendingReviewVOS=new ArrayList<>();
        List<CandidateJobEntity> records = page.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            //候选人id
            List<Long> candidateIds = records.stream().map(CandidateJobEntity::getCandidateId).collect(Collectors.toList());
            //候选人信息
            List<CandidateEntity> candidateList = candidateService.listByIds(candidateIds);
            //ai面试通过记录
            List<Long> candidateJobIds = records.stream().map(CandidateJobEntity::getId).collect(Collectors.toList());
            List<JobStatusRecordEntity> jobStatusRecords = jobStatusRecordService.listByCandidateJobIdsAndApplyStatus(candidateJobIds, JobApplyStatus.REVIEW.getCode());
            //面试结果
            List<AiVettedResultEntity> aiVettedResultList = aiVettedResultService.listByCandidateJobIds(candidateJobIds);
            //自动开关
            Boolean autoFlag=getAutoFlag(vo.getJobId());
            //转换成待人工审核列表数据
            pendingReviewVOS= convertPendingReviewVO(records, candidateList,jobStatusRecords,aiVettedResultList,autoFlag);
        }

        Pager<PendingReviewVO> pager = new Pager<>();
        pager.setCurrentPageRecords(pendingReviewVOS);
        pager.setPageIndex(vo.getPageIndex());
        pager.setPageSize(vo.getPageSize());
        pager.setTotalCount(page.getTotal());
        return pager;
    }

    /**
     * 转换成待人工审核列表
     * @param records 符合要求的成候选人与职位关联表记录
     * @param candidateList 候选人信息
     * @param jobStatusRecords 职位状态记录信息
     * @return
     */
    private List<PendingReviewVO> convertPendingReviewVO(List<CandidateJobEntity> records, List<CandidateEntity> candidateList,
                                                         List<JobStatusRecordEntity> jobStatusRecords,List<AiVettedResultEntity> aiVettedResultList,
                                                         Boolean autoFlag){
        List<PendingReviewVO> reviewVOS=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)){
            List<Long> cityIds=candidateList.stream().map(CandidateEntity::getCityId).toList();
            List<Long> stateIds=candidateList.stream().map(CandidateEntity::getStateId).toList();
            List<Long> countryIds=candidateList.stream().map(CandidateEntity::getCountryId).toList();
            Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(cityIds);
            Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(stateIds);
            Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(countryIds);

            for (CandidateJobEntity candidateJob: records){
                PendingReviewVO vo=new PendingReviewVO();
                vo.setId(candidateJob.getId());
                vo.setCandidateId(candidateJob.getCandidateId());
                //候选人信息
                Optional<CandidateEntity> candidateOptional = candidateList.stream().filter(c -> candidateJob.getCandidateId() != null && candidateJob.getCandidateId().equals(c.getId())).findFirst();
                if (candidateOptional.isPresent()){
                    vo.setCandidateName(candidateOptional.get().getCandidateName());
                    vo.setCandidateEmail(candidateOptional.get().getCandidateEmail());
                    vo.setPhoneNumber(candidateOptional.get().getPhoneNumber());
                    vo.setCountryName(countryMap.get(candidateOptional.get().getCountryId()));
                    vo.setStateName(stateMap.get(candidateOptional.get().getStateId()));
                    vo.setCityName(cityMap.get(candidateOptional.get().getCityId()));
                }
                //ai面试通过时间
                Optional<JobStatusRecordEntity> statusRecordOptional = jobStatusRecords.stream().filter(c -> candidateJob.getId().equals(c.getCandidateJobId())).findFirst();
                statusRecordOptional.ifPresent(jobStatusRecord->vo.setPendingReviewTime(jobStatusRecord.getCreateTime()));
                //候选人各项评分
                CandidateScoreVO candidateScoreVO = convertCandidateScoreVO(candidateJob, aiVettedResultList,autoFlag);
                vo.setCandidateScore(candidateScoreVO);
                reviewVOS.add(vo);
            }
        }
        return reviewVOS;
    }


    /**
     * 获取自动流程-待人工审核列表
     * @param vo
     * @return
     */
    @Override
    public Pager<ManualReviewVO> selectManualReviewPageList(ManualReviewQueryVO vo) {
        CandidateJobQueryVO queryVO= CandidateJobQueryVO.builder().pageIndex(vo.getPageIndex()).pageSize(vo.getPageSize())
                .jobId(vo.getJobId()).applyStatus(JobApplyStatus.MANUAL_REVIEW.getCode()).build();
        //查询符合要求的候选人与职位表记录
        Page<CandidateJobEntity> page=candidateJobService.selectCandidateJobPageList(queryVO);
        //人工审核列表数据
        List<ManualReviewVO> pendingReviewVOS=new ArrayList<>();
        List<CandidateJobEntity> records = page.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            //候选人id
            List<Long> candidateIds = records.stream().map(CandidateJobEntity::getCandidateId).collect(Collectors.toList());
            //候选人信息
            List<CandidateEntity> candidateList = candidateService.listByIds(candidateIds);
            //ai面试通过记录
            List<Long> candidateJobIds = records.stream().map(CandidateJobEntity::getId).collect(Collectors.toList());
            List<JobStatusRecordEntity> jobStatusRecords = jobStatusRecordService.listByCandidateJobIdsAndApplyStatus(candidateJobIds, JobApplyStatus.MANUAL_REVIEW.getCode());
            //ai面试结果
            //面试结果
            List<AiVettedResultEntity> aiVettedResultList = aiVettedResultService.listByCandidateJobIds(candidateJobIds);
            //自动开关
            Boolean autoFlag=getAutoFlag(vo.getJobId());
            //转换成待人工审核列表数据
            pendingReviewVOS= convertManualReviewVO(records, candidateList,jobStatusRecords,aiVettedResultList,autoFlag);
        }

        Pager<ManualReviewVO> pager = new Pager<>();
        pager.setCurrentPageRecords(pendingReviewVOS);
        pager.setPageIndex(vo.getPageIndex());
        pager.setPageSize(vo.getPageSize());
        pager.setTotalCount(page.getTotal());
        return pager;
    }

    /**
     * 转换成自动流程-待人工审核列表
     * @param records 符合要求的成候选人与职位关联表记录
     * @param candidateList 候选人信息
     * @param jobStatusRecords 职位状态记录信息
     * @return
     */
    private List<ManualReviewVO> convertManualReviewVO(List<CandidateJobEntity> records, List<CandidateEntity> candidateList,
                                                       List<JobStatusRecordEntity> jobStatusRecords,List<AiVettedResultEntity> aiVettedResultList,
                                                       Boolean autoFlag){
        List<ManualReviewVO> reviewVOS=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)){
            List<Long> cityIds=candidateList.stream().map(CandidateEntity::getCityId).toList();
            List<Long> stateIds=candidateList.stream().map(CandidateEntity::getStateId).toList();
            List<Long> countryIds=candidateList.stream().map(CandidateEntity::getCountryId).toList();
            Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(cityIds);
            Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(stateIds);
            Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(countryIds);

            for (CandidateJobEntity candidateJob: records){
                ManualReviewVO vo=new ManualReviewVO();
                vo.setId(candidateJob.getId());
                vo.setCandidateId(candidateJob.getCandidateId());
                //候选人信息
                Optional<CandidateEntity> candidateOptional = candidateList.stream().filter(c -> candidateJob.getCandidateId() != null && candidateJob.getCandidateId().equals(c.getId())).findFirst();
                if (candidateOptional.isPresent()){
                    vo.setCandidateName(candidateOptional.get().getCandidateName());
                    vo.setCandidateEmail(candidateOptional.get().getCandidateEmail());
                    vo.setPhoneNumber(candidateOptional.get().getPhoneNumber());
                    vo.setCountryName(countryMap.get(candidateOptional.get().getCountryId()));
                    vo.setStateName(stateMap.get(candidateOptional.get().getStateId()));
                    vo.setCityName(cityMap.get(candidateOptional.get().getCityId()));
                }
                //ai面试通过时间
                Optional<JobStatusRecordEntity> statusRecordOptional = jobStatusRecords.stream().filter(c -> candidateJob.getId().equals(c.getCandidateJobId())).findFirst();
                statusRecordOptional.ifPresent(jobStatusRecord->vo.setReviewTime(jobStatusRecord.getCreateTime()));
                //候选人各项评分
                CandidateScoreVO candidateScoreVO = convertCandidateScoreVO(candidateJob, aiVettedResultList,autoFlag);
                vo.setCandidateScore(candidateScoreVO);
                reviewVOS.add(vo);
            }
        }
        return reviewVOS;
    }


    /**
     * application列表
     * @param vo
     * @return
     */
    public Pager<ApplicationVO> selectApplicationPageList(ApplicationQueryVO vo) {
        CandidateJobQueryVO queryVO= CandidateJobQueryVO.builder().pageIndex(vo.getPageIndex()).pageSize(vo.getPageSize())
                .jobId(vo.getJobId()).score(vo.getScore()).build();
        //查询符合要求的候选人与职位表记录
        Page<CandidateJobEntity> page=candidateJobService.selectCandidateJobPageList(queryVO);
        //列表数据
        List<ApplicationVO> applicationVOS=new ArrayList<>();
        List<CandidateJobEntity> records = page.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            //候选人id
            List<Long> candidateIds = records.stream().map(CandidateJobEntity::getCandidateId).collect(Collectors.toList());
            //候选人信息
            List<CandidateEntity> candidateList = candidateService.listByIds(candidateIds);
            List<Long> candidateJobIds=records.stream().map(CandidateJobEntity::getId).collect(Collectors.toList());
            //候选人面试结果
            List<AiVettedResultEntity> aiVettedResultList = aiVettedResultService.listByCandidateJobIds(candidateJobIds);
            //智能匹配开关
            Boolean autoFlag=getAutoFlag(vo.getJobId());
            //转换成列表数据
            applicationVOS= convertApplicationVO(records, candidateList,aiVettedResultList,autoFlag);
        }

        Pager<ApplicationVO> pager = new Pager<>();
        pager.setCurrentPageRecords(applicationVOS);
        pager.setPageIndex(vo.getPageIndex());
        pager.setPageSize(vo.getPageSize());
        pager.setTotalCount(page.getTotal());
        return pager;
    }

    /**
     * 转换成application列表
     * @param records 符合要求的成候选人与职位关联表记录
     * @param candidateList 候选人信息
     * @return
     */
    private List<ApplicationVO> convertApplicationVO(List<CandidateJobEntity> records,
                                                  List<CandidateEntity> candidateList, List<AiVettedResultEntity> aiVettedResultList,
                                                     Boolean autoFlag){
        List<ApplicationVO> reviewVOS=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)){
            Map<String, CandidateJobEntity> latestCandidateJobMap=new HashMap<>();
            List<Long> cityIds=candidateList.stream().map(CandidateEntity::getCityId).toList();
            List<Long> stateIds=candidateList.stream().map(CandidateEntity::getStateId).toList();
            List<Long> countryIds=candidateList.stream().map(CandidateEntity::getCountryId).toList();
            Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(cityIds);
            Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(stateIds);
            Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(countryIds);

            for (CandidateJobEntity candidateJob: records){
                ApplicationVO vo=new ApplicationVO();
                vo.setId(candidateJob.getId());
                vo.setCandidateId(candidateJob.getCandidateId());
                vo.setSendTime(candidateJob.getCreateTime());
                //候选人信息
                Optional<CandidateEntity> candidateOptional = candidateList.stream().filter(c -> candidateJob.getCandidateId() != null && candidateJob.getCandidateId().equals(c.getId())).findFirst();
                if (candidateOptional.isPresent()){
                    vo.setCandidateName(candidateOptional.get().getCandidateName());
                    vo.setCandidateEmail(candidateOptional.get().getCandidateEmail());
                    vo.setPhoneNumber(candidateOptional.get().getPhoneNumber());
                    vo.setCountryName(countryMap.get(candidateOptional.get().getCountryId()));
                    vo.setStateName(stateMap.get(candidateOptional.get().getStateId()));
                    vo.setCityName(cityMap.get(candidateOptional.get().getCityId()));
                }
                //查询最新的关联记录、判断是否是拒绝状态
                String key=candidateJob.getCandidateId()+"_"+candidateJob.getJobId();
                CandidateJobEntity latestCandidateJob=new CandidateJobEntity();
                if (latestCandidateJobMap.containsKey(key)){
                    latestCandidateJob=latestCandidateJobMap.get(key);
                }else{
                    latestCandidateJob = candidateJobService.getLatestByCandidateIdAndJobId(
                            candidateJob.getCandidateId(), candidateJob.getJobId());
                    latestCandidateJobMap.put(key,latestCandidateJob);
                }
                if (latestCandidateJob.getApplyStatus().equals(JobApplyStatus.DENIED.getCode())){
                    vo.setDisplayReapply(true);
                }else{
                    vo.setDisplayReapply(false);
                }
                vo.setApplyStatus(candidateJob.getApplyStatus());
                vo.setApplyStatusName(JobApplyStatus.fromCode(candidateJob.getApplyStatus()).getName());

                //候选人各项评分
                CandidateScoreVO candidateScoreVO=convertCandidateScoreVO(candidateJob,aiVettedResultList,autoFlag);
                vo.setCandidateScore(candidateScoreVO);
                reviewVOS.add(vo);
            }
        }
        return reviewVOS;
    }

    /**
     * 获取候选人各项得分
     * @param candidateJob
     * @param aiVettedResultList
     * @return
     */
    private CandidateScoreVO convertCandidateScoreVO(CandidateJobEntity candidateJob,List<AiVettedResultEntity> aiVettedResultList,Boolean autoFlag){
        CandidateScoreVO candidateScoreVO=new CandidateScoreVO();
        candidateScoreVO.setResume(candidateJob.getAssessmentScore());
        if (CollectionUtils.isNotEmpty(aiVettedResultList)){
            Optional<AiVettedResultEntity> resultOptional = aiVettedResultList.stream().filter(c -> c.getCandidateJobId().equals(candidateJob.getId())).findFirst();
            if (resultOptional.isPresent()){
                candidateScoreVO.setInterview(resultOptional.get().getInterviewScore());
                candidateScoreVO.setProctoring(resultOptional.get().getProctoringScore());
                candidateScoreVO.setTechnicalSkill(resultOptional.get().getTechnicalSkillScore());
                candidateScoreVO.setSoftSkill(resultOptional.get().getSoftSkillScore());
                if (autoFlag){
                    candidateScoreVO.setOverall(resultOptional.get().getOverallScore());
                }
            }
        }
        String questionInfo = candidateJob.getQuestionInfo();
        if(StringUtils.isNotBlank(questionInfo)) {
            log.debug("candidateJob questionInfo {}", questionInfo);
            CandidateAnswerQuestionInfoDTO answerQuestionInfoDTO = JsonUtils.toObject(questionInfo, CandidateAnswerQuestionInfoDTO.class);
            Integer score = (answerQuestionInfoDTO == null ||
                    answerQuestionInfoDTO.getAnswerQuestion5S() == null ||
                    StringUtils.isBlank(answerQuestionInfoDTO.getAnswerQuestion5S().getScoreFor5S())) ?
                    null : Integer.valueOf(answerQuestionInfoDTO.getAnswerQuestion5S().getScoreFor5S());
            candidateScoreVO.setQuestion5S(score);
        }
        return candidateScoreVO;
    }

    /**
     * 获取拒绝列表
     * @param vo
     * @return
     */
    @Override
    public Pager<DeniedListVO> selectDeniedPageList(CandidateJobQueryVO vo) {
        // 构建ES查询参数，设置拒绝状态过滤
        vo.setApplyStatus(JobApplyStatus.DENIED.getCode());
        // 调用ES服务查询候选人职位信息
        Pager<CandidateJobVO> candidateJobPage = resumeEsService.queryCandidateJobVoList(vo);
        
        // 使用转换器将CandidateJobVO转换为DeniedListVO
        Pager<DeniedListVO> deniedListPage = new Pager<>();
        deniedListPage.setPageIndex(candidateJobPage.getPageIndex());
        deniedListPage.setPageSize(candidateJobPage.getPageSize());
        deniedListPage.setTotalCount(candidateJobPage.getTotalCount());


        // 转换数据列表
        if (CollectionUtils.isNotEmpty(candidateJobPage.getCurrentPageRecords())) {
            List<Long> cityIds=candidateJobPage.getCurrentPageRecords().stream().map(CandidateJobVO::getCityId).toList();
            List<Long> stateIds=candidateJobPage.getCurrentPageRecords().stream().map(CandidateJobVO::getStateId).toList();
            List<Long> countryIds=candidateJobPage.getCurrentPageRecords().stream().map(CandidateJobVO::getCountryId).toList();
            Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(cityIds);
            Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(stateIds);
            Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(countryIds);
            //拒绝记录
            List<Long> candidateJobIds = candidateJobPage.getCurrentPageRecords().stream().map(CandidateJobVO::getId).collect(Collectors.toList());
            List<JobStatusRecordEntity> jobStatusRecords = jobStatusRecordService.listByCandidateJobIdsAndEvent(candidateJobIds, JobApplyStatusEvent.REJECT.toString());
            //候选人面试结果
            List<Long> candidateIds=candidateJobPage.getCurrentPageRecords().stream().map(CandidateJobVO::getId).toList();
            List<AiVettedResultEntity> aiVettedResultList = aiVettedResultService.listByCandidateJobIds(candidateIds);
            //智能匹配开关
            Boolean autoFlag=getAutoFlag(vo.getJobId());

            // 建立candidateJobId到拒绝时间的映射
            Map<Long, LocalDateTime> rejectTimeMap = jobStatusRecords.stream()
                    .collect(Collectors.toMap(
                            JobStatusRecordEntity::getCandidateJobId,
                            JobStatusRecordEntity::getCreateTime,
                            (existing, replacement) -> existing // 如果有重复，保留第一个
                    ));

            List<DeniedListVO> deniedListVOS = DeniedListConverter.INSTANCE
                    .candidateJobVOListToDeniedListVOList(candidateJobPage.getCurrentPageRecords());
            
            // 从jobStatusRecords中设置拒绝时间
            deniedListVOS.forEach(deniedListVO -> {
                deniedListVO.setCityName(cityMap.get(deniedListVO.getCityId()));
                deniedListVO.setStateName(stateMap.get(deniedListVO.getStateId()));
                deniedListVO.setCountryName(countryMap.get(deniedListVO.getCountryId()));
                Long candidateJobId = deniedListVO.getId();
                LocalDateTime rejectTime = rejectTimeMap.get(candidateJobId);
                //候选人各项评分
                CandidateScoreVO candidateScoreVO=new CandidateScoreVO();
                candidateScoreVO.setResume(deniedListVO.getAssessmentScore());
                if (CollectionUtils.isNotEmpty(aiVettedResultList)){
                    Optional<AiVettedResultEntity> resultOptional = aiVettedResultList.stream().filter(c -> c.getCandidateJobId().equals(deniedListVO.getId())).findFirst();
                    if (resultOptional.isPresent()){
                        candidateScoreVO.setInterview(resultOptional.get().getInterviewScore());
                        candidateScoreVO.setProctoring(resultOptional.get().getProctoringScore());
                        candidateScoreVO.setTechnicalSkill(resultOptional.get().getTechnicalSkillScore());
                        candidateScoreVO.setSoftSkill(resultOptional.get().getSoftSkillScore());
                        if (autoFlag){
                            candidateScoreVO.setOverall(resultOptional.get().getOverallScore());
                        }
                    }
                }
                deniedListVO.setCandidateScore(candidateScoreVO);
                deniedListVO.setRejectTime(rejectTime);
            });
            
            deniedListPage.setCurrentPageRecords(deniedListVOS);
        } else {
            deniedListPage.setCurrentPageRecords(new ArrayList<>());
        }
        
        return deniedListPage;
    }

    /**
     * 获取就绪列表
     * @param vo
     * @return
     */
    @Override
    public Pager<ReadyListVO> selectReadyPageList(ReadyQueryVO vo) {
        CandidateJobQueryVO queryVO= CandidateJobQueryVO.builder().pageIndex(vo.getPageIndex()).pageSize(vo.getPageSize())
                .jobId(vo.getJobId()).applyStatus(JobApplyStatus.READY.getCode()).build();
        //查询符合要求的候选人与职位表记录
        Page<CandidateJobEntity> page=candidateJobService.selectCandidateJobPageList(queryVO);
        //拒绝列表数据
        List<ReadyListVO> readyListVOS=new ArrayList<>();
        List<CandidateJobEntity> records = page.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            //候选人id
            List<Long> candidateIds = records.stream().map(CandidateJobEntity::getCandidateId).collect(Collectors.toList());
            //候选人信息
            List<CandidateEntity> candidateList = candidateService.listByIds(candidateIds);
            //就绪记录
            List<Long> candidateJobIds = records.stream().map(CandidateJobEntity::getId).collect(Collectors.toList());
            List<JobStatusRecordEntity> jobStatusRecords = jobStatusRecordService.listByCandidateJobIdsAndApplyStatus(candidateJobIds, JobApplyStatus.READY.getCode());
            //候选人面试结果
            List<AiVettedResultEntity> aiVettedResultList = aiVettedResultService.listByCandidateJobIds(candidateJobIds);
            //自动开关
            Boolean autoFlag=getAutoFlag(vo.getJobId());
            //转换成拒绝列表数据
            readyListVOS= convertReadyListVO(records, candidateList,jobStatusRecords,aiVettedResultList,autoFlag);
        }

        Pager<ReadyListVO> pager = new Pager<>();
        pager.setCurrentPageRecords(readyListVOS);
        pager.setPageIndex(vo.getPageIndex());
        pager.setPageSize(vo.getPageSize());
        pager.setTotalCount(page.getTotal());
        return pager;
    }

    /**
     * 转换成就绪列表数据
     * @param records 符合要求的成候选人与职位关联表记录
     * @param candidateList 候选人信息
     * @param jobStatusRecords 职位状态记录
     * @return
     */
    private List<ReadyListVO> convertReadyListVO(List<CandidateJobEntity> records, List<CandidateEntity> candidateList,
                                                 List<JobStatusRecordEntity> jobStatusRecords,List<AiVettedResultEntity> aiVettedResultList,
                                                 Boolean autoFlag){
        List<ReadyListVO> readyListVOS=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)){
            List<Long> cityIds=candidateList.stream().map(CandidateEntity::getCityId).toList();
            List<Long> stateIds=candidateList.stream().map(CandidateEntity::getStateId).toList();
            List<Long> countryIds=candidateList.stream().map(CandidateEntity::getCountryId).toList();
            Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(cityIds);
            Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(stateIds);
            Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(countryIds);

            for (CandidateJobEntity candidateJob: records){
                ReadyListVO vo=new ReadyListVO();
                vo.setId(candidateJob.getId());
                vo.setCandidateId(candidateJob.getCandidateId());
                //候选人信息
                Optional<CandidateEntity> candidateOptional = candidateList.stream().filter(c -> candidateJob.getCandidateId() != null && candidateJob.getCandidateId().equals(c.getId())).findFirst();
                if (candidateOptional.isPresent()){
                    vo.setCandidateName(candidateOptional.get().getCandidateName());
                    vo.setCandidateEmail(candidateOptional.get().getCandidateEmail());
                    vo.setPhoneNumber(candidateOptional.get().getPhoneNumber());
                    //国家、省、城市
                    vo.setCountryName(countryMap.get(candidateOptional.get().getCountryId()));
                    vo.setStateName(stateMap.get(candidateOptional.get().getStateId()));
                    vo.setCityName(cityMap.get(candidateOptional.get().getCityId()));
                }
                //就绪时间
                Optional<JobStatusRecordEntity> statusRecordOptional = jobStatusRecords.stream().filter(c -> candidateJob.getId().equals(c.getCandidateJobId())).findFirst();
                statusRecordOptional.ifPresent(jobStatusRecord->vo.setReadyTime(jobStatusRecord.getCreateTime()));
                //候选人各项评分
                CandidateScoreVO candidateScoreVO=convertCandidateScoreVO(candidateJob,aiVettedResultList,autoFlag);
                vo.setCandidateScore(candidateScoreVO);
                readyListVOS.add(vo);
            }
        }
        return readyListVOS;
    }

    /**
     * 获取job是否智能匹配
     * @param jobId
     * @return
     */
    private Boolean getAutoFlag(Long jobId){
        List<JobEsEntity> jobEsEntities = jobEsService.listJobIntelligenceScoreRuleByIds(Collections.singletonList(jobId));
        if(CollectionUtils.isNotEmpty(jobEsEntities)) {
            JobEsEntity jobEsEntity = jobEsEntities.getFirst();
            if (jobEsEntity != null && jobEsEntity.getIntelligenceSwitch() != null) {
                return jobEsEntity.getIntelligenceSwitch();
            }
        }
        return false;
    }


    /**
     * ai面试结果页面 通过
     * @param vettedPassDto
     * @return
     */
    @Override
    public Boolean aiVettedPass(VettedPassDto vettedPassDto) {
        jobFlowService.fireEvent(vettedPassDto.getId(), JobApplyStatusEvent.AI_PASS);
        return true;
    }

    /**
     * 人工审核页面 通过
     * @param reviewPassDto
     * @return
     */
    @Override
    public Boolean reviewPass(ReviewPassDto reviewPassDto) {
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();
//        int pricingModel = pointService.getPricingModel(currentUser.getCompanyCode());
//        if (pricingModel==PricingModelEnum.MINUTE.getCode()){
//            CandidateJobEntity candidateJobEntity = candidateJobService.getById(reviewPassDto.getId());
//            //按照面试时长进行扣除积分
//            PointLogVO pointLog = new PointLogVO();
//            pointLog.setUserId(Long.parseLong(currentUser.getId()));
//            pointLog.setCandidateJobId(candidateJobEntity.getId());
//            pointLog.setJobId(candidateJobEntity.getJobId());
//            pointLog.setCandidateId(candidateJobEntity.getCandidateId());
//            pointLog.setTransactionNo(redisSerialNumberUtils.generate(TransactionNoTypeEnum.READY.getCode()));
//            //  换算成分（元 × 100）
//            BigDecimal totalCostFen = minuteBasedConfig.getReadyDeductAmount().multiply(BigDecimal.valueOf(100));
//            // 转成整数（分），四舍五入
//            int totalFen = totalCostFen.setScale(0, RoundingMode.HALF_UP).intValue();
//            // 计算积分（分 × 每分对应积分比例）
//            int totalPoints = totalFen * minuteBasedConfig.getCentExchangeRate();
//            pointLog.setPoints(totalPoints);
//            pointLog.setTransactionType(TransactionTypeEnum.READY.getCode());
//            Boolean flag = pointService.readyDeduct(pointLog);
//            if(!flag){
//                throw new BusinessException(UnChangeResponseCode.INSUFFICIENT_POINTS);
//            }
//        }
        jobFlowService.fireEvent(reviewPassDto.getId(), JobApplyStatusEvent.REVIEW_PASS);
        return true;
    }

    /**
     * 自动流程-人工审核页面 通过
     * @param reviewPassDto
     * @return
     */
    @Override
    public Boolean manualReviewPass(ReviewPassDto reviewPassDto) {
        jobFlowService.fireEvent(reviewPassDto.getId(), JobApplyStatusEvent.AUTO_REVIEW_PASS);
        return true;
    }

    /**
     * 就绪页面 通过
     * @param reviewPassDto
     * @return
     */
    @Override
    public Boolean readyPass(ReadyPassDto reviewPassDto) {
        jobFlowService.fireEvent(reviewPassDto.getId(), JobApplyStatusEvent.BACKGROUND_CHECK);
        return true;
    }

    /**
     * 拒绝候选人
     * @param rejectCandidateDto
     * @return
     */
    @Override
    public Boolean rejectCandidate(RejectCandidateDto rejectCandidateDto) {
        jobFlowService.fireEvent(rejectCandidateDto.getId(), JobApplyStatusEvent.REJECT);
        return true;
    }

    /**
     * 获取面试报告
     * @param candidateJobId
     * @return
     */
    @Override
    public InterviewReportVO getInterviewReport(Long candidateJobId) {
        InterviewReportVO reportVO=new InterviewReportVO();
        if (candidateJobId==null){
            return reportVO;
        }
        reportVO.setId(candidateJobId);

        //查询r_candidate_job获取jobId
        reportVO.setPersonalityTestEnabled(false);
        CandidateJobEntity candidateJobEntity = candidateJobService.getById(candidateJobId);
        if (candidateJobEntity != null && candidateJobEntity.getJobId() != null) {
            //通过jobEsService获取JobEsEntity
            JobEsEntity jobEsEntity = jobEsService.getJobById(candidateJobEntity.getJobId());
            if (jobEsEntity != null) {
                //设置personalityTestEnabled
                reportVO.setPersonalityTestEnabled(jobEsEntity.getPersonalityTestEnabled() != null && jobEsEntity.getPersonalityTestEnabled());
            }
        }

        //ai面试结果信息
        AiVettedResultEntity aiVettedResultEntity = aiVettedResultService.selectByCandidateJobId(candidateJobId);
        if (aiVettedResultEntity!=null){
            reportVO.setInterviewType(aiVettedResultEntity.getInterviewType());
            reportVO.setInterviewScore(aiVettedResultEntity.getInterviewScore());
            reportVO.setCameraRecordingUrl(aiVettedResultEntity.getCameraRecordingUrl());
            reportVO.setSummarizedVideoUrl(aiVettedResultEntity.getSummarizedVideoUrl());
            reportVO.setPhoneRecordingUrl(aiVettedResultEntity.getPhoneRecordingUrl());
            reportVO.setProctoringScore(aiVettedResultEntity.getProctoringScore());
            reportVO.setOverallSkillAssessment(aiVettedResultEntity.getOverallSkillAssessment());
            reportVO.setOverallSkillScore(aiVettedResultEntity.getOverallSkillScore());
            reportVO.setOverallSkillLevel(aiVettedResultEntity.getOverallSkillLevel());
            reportVO.setInterviewTime(aiVettedResultEntity.getInterviewTime());
            reportVO.setTranscript(aiVettedResultEntity.getTranscript());
            if (reportVO.getPersonalityTestEnabled()) {
                reportVO.setPersonalityScore(aiVettedResultEntity.getPersonalityScore() == null ? 0 : aiVettedResultEntity.getPersonalityScore());
            }
            //判断数据来源
            if(aiVettedResultEntity.getDataSource()== DataSourceEnum.PHL.getSource()){
                reportVO.setCallId(DataSourceEnum.PHL.getName()+candidateJobId);
                reportVO.setCameraRecordingUrl(s3Utils.generatePresignedUrl(aiVettedResultEntity.getCameraRecordingUrl(),expireSeconds));
                reportVO.setInterviewReportUrl(s3Utils.generatePresignedUrl(aiVettedResultEntity.getInterviewReportUrl(),expireSeconds));
            }else{
                reportVO.setCallId(aiService.getCallId(candidateJobId));
                reportVO.setInterviewReportUrl(s3Utils.generatePresignedUrl(aiVettedResultEntity.getInterviewReportUrl(),expireSeconds));
            }
            //ai面试结果技能信息
            List<AiVettedResultSkillEntity> aiVettedResultSkills = aiVettedResultSkillService.listByVettedResultIds(Collections.singletonList(aiVettedResultEntity.getId()));
            if (CollectionUtils.isNotEmpty(aiVettedResultSkills)) {
                // 分离软技能和其他技能
                Optional<AiVettedResultSkillEntity> softSkill = aiVettedResultSkills.stream()
                        .filter(skill -> StringUtils.equals(skill.getSkillName(), CommonConstants.AI_VETTED_SOFT_SKILL_NAME))
                        .findFirst();
                // 设置软技能
                softSkill.ifPresent(skillEntity -> {
                    AiVettedResultSkillVO skillVo = AiVettedResultSkillConverter.INSTANCE.toSkillVo(skillEntity);
                    skillVo.setLevelName(SoftSkillLevelEnum.getNameByCode(skillEntity.getSkillLevel()));
                    reportVO.setSoftSkill(skillVo);
                });
                // 过滤掉软技能，保留其他技能
                List<AiVettedResultSkillVO> skillVOS = aiVettedResultSkills.stream()
                        .filter(skill -> !StringUtils.equals(skill.getSkillName(), CommonConstants.AI_VETTED_SOFT_SKILL_NAME))
                        .map(AiVettedResultSkillConverter.INSTANCE::toSkillVo)
                        .collect(Collectors.toList());

                reportVO.setSkillList(skillVOS);
            }
        }
        return reportVO;
    }

    /**
     * 获取笔试结果
     * @param candidateJobId
     * @return
     */
    @Override
    public WrittenDetailVO getVettedWrittenResult(Long candidateJobId) {
        WrittenDetailVO writtenDetailVO=new WrittenDetailVO();
        WrittenTestAnalysisVO writtenTestAnalysisVO = new WrittenTestAnalysisVO();
        AiVettedResultEntity aiVettedResultEntity = aiVettedResultService.selectByCandidateJobId(candidateJobId);
        if (aiVettedResultEntity!=null){
            writtenTestAnalysisVO.setScore(aiVettedResultEntity.getOverallSkillScore());
            writtenTestAnalysisVO.setEvaluationSummary(aiVettedResultEntity.getOverallSkillAssessment());
            writtenTestAnalysisVO.setSkillLevel(aiVettedResultEntity.getOverallSkillLevel());
        }
        writtenDetailVO.setWrittenTestAnalysis(writtenTestAnalysisVO);
        return writtenDetailVO;
    }

    /**
     * 候选人应聘流程时间线
     * @param candidateJobId
     * @return
     */
    @Override
    public CandidateProcessVO getCandidateProcess(Long candidateJobId) {
        CandidateProcessVO processVO=new CandidateProcessVO();
        if (candidateJobId==null){
            return processVO;
        }
        //候选人职位关联
        CandidateJobEntity candidateJob = candidateJobService.getById(candidateJobId);
        if (candidateJob!=null){
            processVO.setJobId(candidateJob.getJobId());
            //候选人信息
            processAddCandidate(processVO,candidateJob.getCandidateId());
            //职位信息
            JobEsEntity job = jobEsService.getJobById(candidateJob.getJobId());
            if (job!=null){
                processVO.setJobTitle(job.getTitle());
                processVO.setJobCreateTime(job.getCreateTime());
                if (StringUtils.isNotBlank(candidateJob.getPlaceId())) {
                    Map<String, String> placeMap = locationService.listIdNameMapByPlaceIds(Collections.singletonList(candidateJob.getPlaceId()));
                    processVO.setJobLocationName(placeMap.get(candidateJob.getPlaceId()));
                } else {
                    Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(Collections.singletonList(candidateJob.getJobCityId()));
                    Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(Collections.singletonList(candidateJob.getJobStateId()));
                    Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(Collections.singletonList(candidateJob.getJobCountryId()));
                    processVO.setJobLocationName(CommonUtils.getLocationName(countryMap.get(candidateJob.getJobCountryId()),stateMap.get(candidateJob.getJobStateId()),cityMap.get(candidateJob.getJobCityId())));
                }
            }
            //时间线
            processAddLine(processVO,candidateJobId);
        }
        return processVO;
    }

    /**
     * 候选人应聘流程时间线-增加候选人信息
     * @param processVO
     * @param candidateId
     */
    private void processAddCandidate(CandidateProcessVO processVO,Long candidateId){
        processVO.setCandidateId(candidateId);
        CandidateEntity candidateEntity = candidateService.getById(candidateId);
        if (candidateEntity!=null){
            processVO.setCandidateName(candidateEntity.getCandidateName());
            processVO.setExpectedSalary(candidateEntity.getExpectedSalary());
            //薪资类型、货币类型
            List<DictionaryDTO> dictionaryDTOS = dictionaryService.listDtoByIds(Arrays.asList(candidateEntity.getSalaryTypeId(),candidateEntity.getCurrencyTypeId()));
            Map<Long,String> dictionaryMap=dictionaryDTOS.stream().collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue));
            processVO.setSalaryType(dictionaryMap.get(candidateEntity.getSalaryTypeId()));
            processVO.setCurrencyType(dictionaryMap.get(candidateEntity.getCurrencyTypeId()));
            //国家、省、市
            Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(Collections.singletonList(candidateEntity.getCityId()));
            Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(Collections.singletonList(candidateEntity.getStateId()));
            Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(Collections.singletonList(candidateEntity.getCountryId()));
            processVO.setCountryName(countryMap.get(candidateEntity.getCountryId()));
            processVO.setStateName(stateMap.get(candidateEntity.getStateId()));
            processVO.setCityName(cityMap.get(candidateEntity.getCityId()));
            processVO.setAvailableFrom(candidateEntity.getAvailableFrom());
        }
    }


    /**
     * 候选人应聘流程时间线-增加时间线
     * @param processVO
     * @param candidateJobId
     */
    private void processAddLine(CandidateProcessVO processVO,Long candidateJobId){
        List<ProcessTimeLineVO> timeLineList=new ArrayList<>();
        List<JobStatusRecordEntity> recordEntities=jobStatusRecordService.listByCandidateJobId(candidateJobId);
        if (CollectionUtils.isNotEmpty(recordEntities)){
            for (JobStatusRecordEntity recordEntity:recordEntities){
                ProcessTimeLineVO lineVO=new ProcessTimeLineVO();
                lineVO.setEvent(recordEntity.getApplyEvent());
                lineVO.setEventTime(recordEntity.getCreateTime());
                timeLineList.add(lineVO);
            }
        }
        processVO.setTimeLineList(timeLineList);
    }

    /**
     * 候选人详细信息
     * @param id 候选人id
     * @return
     */
    @Override
    public CandidateDetailsVO getCandidateDetails(Long id) {
        CandidateDetailsVO candidateDetailsVO=new CandidateDetailsVO();
        if (id==null){
            return candidateDetailsVO;
        }
        CandidateEntity candidateEntity = candidateService.getById(id);
        if (candidateEntity==null){
            return candidateDetailsVO;
        }
        try {
            candidateEntity.setResumeUrl(s3Utils.generatePresignedUrl(candidateEntity.getResumeUrl(),3600*24));
        } catch (Exception e) {
            candidateEntity.setResumeUrl("");
        }
        //候选人基本信息
        candidateDetailsVO = CandidateConverter.INSTANCE.convertEntityToDetailsVO(candidateEntity);
        //字典查询
        Map<Long, String> dictionaryMap = dictionaryService
                .listByTypes(Arrays.asList(DictionaryEnum.DEGREE_TYPE.getName(),DictionaryEnum.COMPLETION.getName()
                        ,DictionaryEnum.CAMER_RECORDING_URL.getName(),DictionaryEnum.REPORT.getName(),DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()))
                .stream()
                .collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue));
        //从es中获取教育经历、工作经历
        CandidateEsEntity candidateEsEntity = resumeEsService.searchResumeToEs(id);
        if (CommonUtils.isLong(candidateEntity.getGender()) && dictionaryMap.containsKey(Long.valueOf(candidateEntity.getGender()))){
            candidateDetailsVO.setGender(dictionaryMap.get(Long.valueOf(candidateEntity.getGender())));
        }
        candidateDetailsVO.setCurrencyTypeName(dictionaryMap.get(candidateEntity.getCurrencyTypeId()));
        candidateDetailsVO.setSalaryTypeName(dictionaryMap.get(candidateEntity.getSalaryTypeId()));
        if (candidateEsEntity!=null){
            // 教育经历
            List<CandidateEducationEntity> educations = candidateEsEntity.getCandidateEducations();
            if (CollectionUtils.isNotEmpty(educations)) {
                List<EducationDetailsVO> educationList = educations.stream()
                        .map(edu -> {
                            EducationDetailsVO vo = CandidateEducationConverter.INSTANCE.convertEntityToDetailsVO(edu);
                            vo.setDegreeName(dictionaryMap.get(edu.getDegreeId()));
                            vo.setInstitutionName(dictionaryMap.get(edu.getInstitutionTypeId()));
                            return vo;
                        })
                        .collect(Collectors.toList());
                candidateDetailsVO.setEducationList(educationList);
            }
            // 工作经历
            List<EmploymentHistoryEntity> employmentHistories = candidateEsEntity.getEmploymentHistories();
            if (CollectionUtils.isNotEmpty(employmentHistories)) {
                List<EmploymentDetailsVO> employmentList = EmploymentHistoryConverter.INSTANCE.entityListToDetailsVOList(employmentHistories);
                candidateDetailsVO.setEmploymentList(employmentList);
            }
            if (candidateEsEntity.getCountryId()!=null){
                List<CountryDTO> countryDTOS = locationService.listByCountryIds(List.of(candidateEsEntity.getCountryId()));
                if (CollectionUtils.isNotEmpty(countryDTOS)){
                    candidateDetailsVO.setCountryName(countryDTOS.getFirst().getName());
                }
            }
            if(candidateEsEntity.getStateId()!=null){
                List<StateDTO> stateDTOS = locationService.listByStateIds(List.of(candidateEsEntity.getStateId()));
                if (CollectionUtils.isNotEmpty(stateDTOS)){
                    candidateDetailsVO.setStateName(stateDTOS.getFirst().getName());
                }
            }
            if(candidateEsEntity.getCityId()!=null){
                List<CityDTO> cityDTOS = locationService.listByCityIds(List.of(candidateEsEntity.getCityId()));
                if (CollectionUtils.isNotEmpty(cityDTOS)){
                    candidateDetailsVO.setCityName(cityDTOS.getFirst().getName());
                }
            }
        }
        candidateDetailsVO.setCandidateId(candidateEntity.getId());
        return candidateDetailsVO;
    }

    /**
     * Get candidate details by encrypted share ID
     * Decrypt the encrypted candidate ID from share link and retrieve candidate details
     * This method is used by recruiters to view shared candidate information
     *
     * @param encryptedId Encrypted candidate ID from share link
     * @return Candidate details VO with complete information
     */
    @Override
    public CandidateDetailsVO getCandidateDetailsByShareId(String encryptedId) {
        log.info("Getting candidate details by share ID encryptedId: {}", encryptedId);

        // 1. Validate input
        if (StringUtils.isBlank(encryptedId)) {
            log.warn("Encrypted candidate ID is blank");
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_SHARE_LINK_INVALID);
        }

        // 2. Decrypt candidate ID using ShortIdGenerator
        Long candidateId = shortIdGenerator.decryptCandidateId(encryptedId);
        log.info("Getting candidate details by share ID: {}", candidateId);
        // 3. Validate decryption result
        if (candidateId == null) {
            log.warn("Failed to decrypt candidate ID: {}", encryptedId);
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_SHARE_LINK_INVALID);
        }

        log.info("Decrypted candidate ID: {} from encrypted ID: {}",
            candidateId, encryptedId);

        // 4. Call existing getCandidateDetails method
        CandidateDetailsVO candidateDetails = getCandidateDetails(candidateId);

        // 5. Validate result (getCandidateDetails may return empty VO if not found)
        if (candidateDetails == null ||
            StringUtils.isBlank(candidateDetails.getCandidateEmail())) {
            log.warn("Candidate not found for ID: {}", candidateId);
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
        }

        log.info("Successfully retrieved candidate details for encrypted ID: {}",
            encryptedId);

        return candidateDetails;
    }

    /**
     * 发送邮件 扣积分
     * @param candidateJob
     */
    @Override
    public void processInterviewMail(CandidateJobEntity candidateJob,Long userId, boolean isAutoTrigger){
        log.info("Processing interview mail for candidateJobId: {}", candidateJob.getId());
        JobEntity job = jobService.getById(candidateJob.getJobId());
        if (job == null) {
            log.warn("Job not found for jobId: {}, skipping candidateJobId: {}",
                    candidateJob.getJobId(), candidateJob.getId());
            return;
        }
        if(StringUtils.isEmpty(job.getInterviewUrlId())){
            throw new BusinessException(CommonResponseCode.INTERVIEW_ID_FAILED);
        }
        if (InterviewTypeEnum.AI_PHONE.getCode() == job.getInterviewType()) {
            checkInterviewTime(candidateJob);
        }
        //校验是否已经冻结积分
        PointsOperationLog pointsOperationLog = pointsOperationLogService.selectinterviewFreezeLog(candidateJob.getId());
        if (pointsOperationLog!=null){
            return;
        }
        RLock lock = redissonClient.getLock("processInterviewMail:"+candidateJob);
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
                //是否豁免积分
                boolean isExempt=pointService.isExempt(job.getCompanyCode());
                if (isExempt){
                    log.info("Company {} is exempt from interview mail points deduction", job.getCompanyCode());
                    // 发送邮件（自动触发，根据岗位地理位置选择模板）
                    processAiInterview(candidateJob, isAutoTrigger);
                }else{
                    //积分消费模式
                    int pricingModel = pointService.getPricingModel(job.getCompanyCode());
                    Long userPrimaryId = pointService.getUserPrimaryId(job.getCompanyCode());
                    if (userPrimaryId==null){
                        throw new BusinessException(UnChangeResponseCode.PRIMARY_ACCOUNT_NOT_FOUND);
                    }
                    // 创建积分操作对象
                    PointLogVO emailPointLog = createEmailPointLog(candidateJob, job,userPrimaryId,userId);
                    PointLogVO interviewPointLog = createInterviewPointLog(candidateJob, job,pricingModel,userPrimaryId,userId);

                    try {
                        // 冻结积分
                        freezePoints(emailPointLog, interviewPointLog);
                        // 发送邮件（自动触发，根据岗位地理位置选择模板）
                        processAiInterview(candidateJob, isAutoTrigger);
                        if(InterviewMailStatusEnum.SEND.getCode().equals(candidateJob.getInterviewMailStatus())
                                || InterviewPhoneStatusEnum.SCHEDULED.getCode().equals(candidateJob.getInterviewPhoneStatus())){
//                pointService.confirmPointsFreeze(emailPointLog);
//                log.info("Email points deducted successfully for candidateJobId: {}", candidateJob.getId());
                        }else{
                            log.info("Email points deducted error for candidateJobId: {}", candidateJob.getId());
                            //邮件未发送成功，则取消积分占用
                            cancelAllPointsFreeze(emailPointLog, interviewPointLog);
                            throw new BusinessException(CommonResponseCode.INTERVIEW_EMAIL_FAILED);
                        }
                    } catch (Exception e) {
                        // 发生异常时取消所有积分冻结
                        cancelAllPointsFreeze(emailPointLog, interviewPointLog);
                        throw e;
                    }
                }
            }else {
                throw BusinessException.of(COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER);
            }
        } catch (Exception e){
            log.error("processInterviewMail candidateJobId:{},error:",candidateJob.getId(),e);
            throw e;
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    /**
     * 创建邮件积分日志
     */
    private PointLogVO createEmailPointLog(CandidateJobEntity candidateJob, JobEntity job,Long userId,Long operUserId) {
        PointLogVO pointLog = new PointLogVO();
        pointLog.setUserId(userId);
        pointLog.setOperUserId(operUserId!=null?operUserId:1);
        pointLog.setCandidateJobId(candidateJob.getId());
        pointLog.setJobId(job.getId());
        pointLog.setCandidateId(candidateJob.getCandidateId());
        pointLog.setTransactionNo(redisSerialNumberUtils.generate(TransactionNoTypeEnum.INTERVIEW_MAIL.getCode()));
        pointLog.setPoints(businessDeductionPointsConfig.getAiInterview().getEmailDeductedPoints());
        pointLog.setTransactionType(TransactionTypeEnum.INTERVIEW_MAIL.getCode());
        pointLog.setCompanyCode(job.getCompanyCode());
        return pointLog;
    }

    /**
     * 创建面试积分日志
     */
    private PointLogVO createInterviewPointLog(CandidateJobEntity candidateJob, JobEntity job,int pricingModel,Long userId,Long operUserId) {
        PointLogVO pointLog = new PointLogVO();
        pointLog.setUserId(userId);
        pointLog.setOperUserId(operUserId!=null?operUserId:1);
        pointLog.setCandidateJobId(candidateJob.getId());
        pointLog.setJobId(job.getId());
        pointLog.setCandidateId(candidateJob.getCandidateId());
        pointLog.setFreezeInterviewType(job.getInterviewType());
        pointLog.setTransactionNo(redisSerialNumberUtils.generate(TransactionNoTypeEnum.AI_INTERVIEW.getCode()));
        if (pricingModel== PricingModelEnum.TOKEN.getCode()){
            pointLog.setPoints(businessDeductionPointsConfig.getAiInterview().getInterviewFreezePoints());
        }else if (pricingModel== PricingModelEnum.MINUTE.getCode()){
            //计算冻结积分
            if (job.getInterviewType() == InterviewTypeEnum.AI_PHONE.getCode() || job.getInterviewType()== InterviewTypeEnum.VIDEO.getCode()){
                Integer points= CommonUtils.getInterviewFreezePoints(minuteBasedConfig.getCentExchangeRate(),job.getInterviewLength(),minuteBasedConfig.getInterviewCostPerMinute());
                pointLog.setPoints(points);
            }else if(job.getInterviewType()== InterviewTypeEnum.AUDIO.getCode()){
                Integer points= CommonUtils.getInterviewFreezePoints(minuteBasedConfig.getCentExchangeRate(),job.getInterviewLength(),minuteBasedConfig.getAudioInterviewCostPerMinute());
                pointLog.setPoints(points);
            }
        }
        pointLog.setTransactionType(TransactionTypeEnum.AI_INTERVIEW.getCode());
        pointLog.setCompanyCode(job.getCompanyCode());
        return pointLog;
    }

    /**
     * 冻结积分
     */
    private void freezePoints(PointLogVO emailPointLog, PointLogVO interviewPointLog) {
//        // 冻结邮件积分
//        if (!pointService.checkPointsFreeze(emailPointLog)) {
//            throw new BusinessException(UnChangeResponseCode.INSUFFICIENT_POINTS);
//        }

        // 冻结面试积分
        if (!pointService.checkPointsFreeze(interviewPointLog)) {
            // 取消邮件积分冻结
            pointService.cancelPointsFreeze(emailPointLog);
            throw new BusinessException(UnChangeResponseCode.INSUFFICIENT_POINTS);
        }
    }


    /**
     * 取消所有积分冻结
     */
    private void cancelAllPointsFreeze(PointLogVO emailPointLog, PointLogVO interviewPointLog) {
        try {
//            pointService.cancelPointsFreeze(emailPointLog);
            pointService.cancelPointsFreeze(interviewPointLog);
            log.info("All points freeze cancelled for candidateJobId: {}", emailPointLog.getCandidateJobId());
        } catch (Exception e) {
            log.error("Failed to cancel points freeze for candidateJobId: {}",
                    emailPointLog.getCandidateJobId(), e);
        }
    }

    /**
     * 发送重新申请邀请邮件
     * @param candidateJobId 候选人职位关联ID
     * @return 是否发送成功
     */
    @Override
    public Boolean sendReapplyInvitation(Long candidateJobId) {
        try {
            // 获取候选人职位关联信息
            CandidateJobEntity candidateJob = candidateJobService.getById(candidateJobId);
            if (candidateJob == null) {
                log.warn("CandidateJob not found for id: {}", candidateJobId);
                return false;
            }

            // 根据candidateId和jobId查询最新记录，检查是否已经发送过邀请
            CandidateJobEntity latestCandidateJob = candidateJobService.getLatestByCandidateIdAndJobId(
                candidateJob.getCandidateId(), candidateJob.getJobId());
            if (latestCandidateJob==null){
                throw new BusinessException(CandidateResponseCode.NOT_FOUND_DELIVERY_RECORD);
            }

            if (!latestCandidateJob.getApplyStatus().equals(JobApplyStatus.DENIED.getCode())){
                throw new BusinessException(CandidateResponseCode.NOT_ALLOWED_REAPPLY);
            }

//            if (ReapplyInvitedEnum.isInvited(latestCandidateJob.getReapplyInvited())) {
//                throw new BusinessException(CandidateResponseCode.CANDIDATE_NOT_APPLY_REPEATEDLY);
//            }

            // 获取候选人信息
            CandidateEntity candidate = candidateService.getById(candidateJob.getCandidateId());
            if (candidate == null) {
                log.warn("Candidate not found for id: {}", candidateJob.getCandidateId());
                return false;
            }

            // 获取职位信息
            JobEntity job = jobService.getById(candidateJob.getJobId());
            if (job == null) {
                log.warn("Job not found for id: {}", candidateJob.getJobId());
                return false;
            }

            // 准备邮件模板参数
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("candidateName", candidate.getCandidateName());
            templateParams.put("jobTitle", job.getTitle());

            // 添加公司名称（使用companyCode或默认值）
            String companyName = "";
            CompanyInfoSimpleDTO companyInfoSimpleDTO = companyService.getCompanyInfoByCode(job.getCompanyCode());
            if(companyInfoSimpleDTO!=null){
                companyName=companyInfoSimpleDTO.getName();
            }
            templateParams.put("companyName", companyName);

            // 构建职位URL（使用urlCode构建前端链接）
            String urlCode = generateUrlCodeService.generateUrlCodeByConfig(job.getTitle(), job.getUrlCode(), companyName, companyNameReplace);
            String idStr = shortIdGenerator.generateShortId(job.getId());
            String shareLink = jobDomainService.generateInfoShareLink(idStr, urlCode);
            templateParams.put("jobUrl", shareLink);

            // 根据语言环境选择模板和主题
            String templateName = LanguageLocalUtils.getEmailTemplateName("ReapplyInvitation.html");
            Locale locale = UserContextUtil.getLanguageLocal();
//            boolean chinese = LanguageLocalUtils.isChinese(locale);
//            String subject = chinese
//                ? "重新申请邀请 - " + job.getTitle()
//                : "Reapply Invitation - " + job.getTitle();
            String subject = LanguageLocalUtils.getReapplyForInvitationEmailSubject(locale) + job.getTitle();
            
            // 发送邮件
            mailUtils.sendHtmlTemplateMail(
                candidate.getCandidateEmail(),
                subject,
                templateName,
                templateParams
            );

            // 更新最新记录的邀请状态
            latestCandidateJob.setReapplyInvited(ReapplyInvitedEnum.INVITED.getCode());
            candidateJobService.updateById(latestCandidateJob);
            log.info("Reapply invitation sent successfully for candidateJobId: {}", candidateJobId);
            return true;
        } catch (BusinessException e) {
            log.warn("Business exception when sending reapply invitation for candidateJobId: {}, error: {}",
                candidateJobId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to send reapply invitation for candidateJobId: {}", candidateJobId, e);
            throw new BusinessException(CandidateResponseCode.EMAIL_SEND_FAILURE);
        }
    }

    /**
     * 检查职位是否可以申请
     * @param jobId 职位ID
     * @return 是否可以申请
     */
    @Override
    public Boolean checkApplicationJob(Long jobId) {
        if(jobId==null){
            return false;
        }

        IamUserContextDTO candidateNeedLogin = UserContextUtil.getCurrentUserCandidateNeedLogin();
        CandidateEntity candidateEntity = candidateService.getCandidateByCandidateId(Long.parseLong(candidateNeedLogin.getId()));
        if (candidateEntity == null) {
            return false;
        }
        //校验简历上传状态 简历状态等于0表示上传失败
        if (candidateEntity.getUploadStatus() != null && candidateEntity.getUploadStatus() == 0) {
            return false;
        }
        //校验职位数据
        JobEntity jobEntity = jobService.getJobsByIds(jobId);
        if (jobEntity == null) {
            return false;
        }
        //校验职位状态是否是active（open）
        if (JobStatus.getByCode(jobEntity.getJobStatus()) != ACTIVE) {
            return false;
        }
        //校验职位的companyCode属性是否存在
        if (StringUtils.isBlank(jobEntity.getCompanyCode())) {
           return false;
        }
        //获取最新记录是否允许再申请
        CandidateJobEntity latestByCandidateIdAndJob = candidateJobService.getLatestByCandidateIdAndJobId(candidateEntity.getId(), jobId);
        if (repeatApplyJobLimitEnable && !checkAllowApply(latestByCandidateIdAndJob)) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean validateInterviewTime(Long candidateId, OffsetDateTime preferredInterviewStartTime, OffsetDateTime preferredInterviewEndTime) {
        // Validate input parameters
        if (candidateId == null) {
            throw BusinessException.of(GlobalStatusCode.PARAM_ERROR, "Candidate ID is required.");
        }
        if (preferredInterviewStartTime == null || preferredInterviewEndTime == null) {
            throw BusinessException.of(GlobalStatusCode.PARAM_ERROR, "Interview start time and end time are required.");
        }

        // Validate that start time must be after current time
        // Use UTC time for comparison to ensure consistency across different timezones
        // OffsetDateTime comparison automatically handles timezone differences by converting to UTC internally
        OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC);
        if (preferredInterviewStartTime.isBefore(currentTime) || preferredInterviewStartTime.isEqual(currentTime)) {
            throw BusinessException.of(GlobalStatusCode.PARAM_ERROR, "Interview start time must be after current time.");
        }

        // Validate that end time must be after start time
        if (preferredInterviewStartTime.isAfter(preferredInterviewEndTime) || preferredInterviewStartTime.isEqual(preferredInterviewEndTime)) {
            throw BusinessException.of(GlobalStatusCode.PARAM_ERROR, "Interview end time must be after start time.");
        }

        // Query all candidate job records for this candidate
        LambdaQueryWrapper<CandidateJobEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateJobEntity::getCandidateId, candidateId)
                .isNull(CandidateJobEntity::getInterviewTime)
                .lt(CandidateJobEntity::getApplyStatus, JobApplyStatus.VETTED.getCode());
        List<CandidateJobEntity> candidateJobList = candidateJobService.list(queryWrapper);

        if (CollectionUtils.isEmpty(candidateJobList)) {
            // No existing interview time records, no conflict
            return true;
        }

        // Check for time conflicts
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        for (CandidateJobEntity candidateJob : candidateJobList) {
            String existingStartTimeStr = candidateJob.getPreferredInterviewStartTime();
            String existingEndTimeStr = candidateJob.getPreferredInterviewEndTime();

            if (StringUtils.isBlank(existingStartTimeStr) || StringUtils.isBlank(existingEndTimeStr)) {
                continue;
            }

            try {
                // Parse String to OffsetDateTime
                OffsetDateTime existingStartTime = OffsetDateTime.parse(existingStartTimeStr, formatter);
                OffsetDateTime existingEndTime = OffsetDateTime.parse(existingEndTimeStr, formatter);

                // Check if time ranges overlap
                // Two time ranges [start1, end1] and [start2, end2] overlap if: start1 < end2 && start2 < end1
                if (preferredInterviewStartTime.isBefore(existingEndTime) && existingStartTime.isBefore(preferredInterviewEndTime)) {
                    log.warn("Interview time conflict detected: candidateId={}, existing time range=[{}, {}], new time range=[{}, {}]",
                            candidateId, existingStartTime, existingEndTime, preferredInterviewStartTime, preferredInterviewEndTime);

                    // Format time range for error message using user's timezone for friendly display
                    // Convert to user's timezone (same as preferredInterviewStartTime) and format as yyyy-MM-dd HH:mm:ss
                    ZoneOffset userOffset = preferredInterviewStartTime.getOffset();
                    DateTimeFormatter friendlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedStartTime = existingStartTime.withOffsetSameInstant(userOffset).format(friendlyFormatter);
                    String formattedEndTime = existingEndTime.withOffsetSameInstant(userOffset).format(friendlyFormatter);
                    String errorMessage = String.format("Interview time conflict with %s - %s", formattedStartTime, formattedEndTime);
                    throw BusinessException.of(CandidateResponseCode.INTERVIEW_TIME_CONFLICT.getCode(), errorMessage);
                }
            } catch (Exception e) {
                // If parsing fails, log warning but continue checking other records
                log.warn("Failed to parse interview time for candidateJobId={}, startTime={}, endTime={}, error={}",
                        candidateJob.getId(), existingStartTimeStr, existingEndTimeStr, e.getMessage());
                throw e;
            }
        }

        // No conflicts found
        return true;
    }

    /**
     * Generate encrypted candidate ID for sharing
     * Validates candidate existence and encrypts candidate ID
     *
     * @param candidateId Candidate ID to encrypt
     * @return Encrypted candidate ID string
     */
    /**
     * 判断岗位地理位置是否在中国
     * 根据岗位的国家ID判断，如果国家ISO2代码为"CN"或国家名称为"China"或"中国"，则返回true
     *
     * @param candidateJobEntity 候选人职位关联实体
     * @return true if job location is in China, false otherwise
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
            
            // 判断是否为中国的几种方式：
            // 1. ISO2代码为"CN"
            // 2. 国家名称为"China"或"中国"
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

    @Override
    public CandidateShareVO generateCandidateShareLink(Long candidateId) {
        log.info("Generating encrypted candidate ID for candidateId: {}", candidateId);

        // 1. Validate candidateId
        if (candidateId == null || candidateId <= 0) {
            log.warn("Invalid candidate ID: {}", candidateId);
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
        }

        // 2. Verify candidate exists
        CandidateEntity candidate = candidateService.getById(candidateId);
        if (candidate == null) {
            log.warn("Candidate not found for ID: {}", candidateId);
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
        }

        // 3. Encrypt candidate ID
        String encryptedId = shortIdGenerator.encryptCandidateId(candidateId);

        log.info("Generated encrypted candidate ID: {} for candidateId: {}",
                encryptedId, candidateId);
        CandidateShareVO candidateShareVO = new CandidateShareVO();
        candidateShareVO.setShareId(encryptedId);
        return candidateShareVO;
    }


    /**
     * 视频跳转中转
     * @param applicationId
     * @param response
     */
    @Override
    public void redirectToVideo(String applicationId, HttpServletResponse response) {
        Optional<DataMigrationMappingEntity> mappingEntityOptional = dataMigrationMappingService.findByPgsqlIdAndType(applicationId, MigrationBusTypeEnum.INTERVIEW_REPORTS);
        if (mappingEntityOptional.isPresent()){
            Long candidateJobId = mappingEntityOptional.get().getMysqlId();
            AiVettedResultEntity aiVettedResultEntity = aiVettedResultService.selectByCandidateJobId(candidateJobId);
            if (aiVettedResultEntity!=null){
                String signedUrl=s3Utils.generatePresignedUrl(aiVettedResultEntity.getCameraRecordingUrl(),3600*24);
                try {
                    response.sendRedirect(signedUrl);
                } catch (IOException e) {
                    log.error("redirectToVideo applicationId={}, error={}",applicationId,e.getMessage());
                }
            }
        }
    }

    @Override
    public void sendInterviewReportMailToCandidate(Long candidateJobId) {
        try {
            log.info("Starting to send interview report email for candidateJobId: {}", candidateJobId);

            // 1. 获取候选人职位关联信息
            CandidateJobEntity candidateJobEntity = candidateJobService.getById(candidateJobId);
            if (candidateJobEntity == null) {
                log.warn("CandidateJob not found for id: {}", candidateJobId);
                return;
            }

            // 2. 获取候选人信息
            CandidateEntity candidateEntity = candidateService.getById(candidateJobEntity.getCandidateId());
            if (candidateEntity == null) {
                log.warn("Candidate not found for id: {}", candidateJobEntity.getCandidateId());
                return;
            }

            // 3. 获取职位信息
            JobEntity jobEntity = jobService.getJobsByIds(candidateJobEntity.getJobId());
            if (jobEntity == null) {
                log.warn("Job not found for id: {}", candidateJobEntity.getJobId());
                return;
            }

            // 4. 获取面试报告信息
            InterviewReportVO interviewReport = getInterviewReport(candidateJobId);
            if (interviewReport == null || StringUtils.isEmpty(interviewReport.getInterviewReportUrl())) {
                log.warn("Interview report not found or report URL is empty for candidateJobId: {}", candidateJobId);
                throw new BusinessException(CandidateResponseCode.INTERVIEW_REPORT_NOT_FOUND);
            }

            // 5. 获取公司信息
            IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(jobEntity.getCompanyCode());
            if (companyInfo == null) {
                log.warn("Company info not found for companyCode: {}", jobEntity.getCompanyCode());
                return;
            }

            // 6. 生成面试报告URL（如果URL是S3 key，需要生成预签名URL）
            String reportUrl = interviewReport.getInterviewReportUrl();
            boolean isPresignedUrl = false;
            // 如果URL是S3 key格式（不包含http），则生成预签名URL
            if (!reportUrl.startsWith("http://") && !reportUrl.startsWith("https://")) {
                reportUrl = s3Utils.generatePresignedUrl(reportUrl, reportUrlExpireSeconds);
                isPresignedUrl = true;
            }

            // 7. 准备邮件模板参数
            Map<String, Object> templateParams = new java.util.HashMap<>();
            templateParams.put("candidateName", candidateEntity.getCandidateName());
            templateParams.put("jobTitle", jobEntity.getTitle());
            templateParams.put("companyName", companyInfo.getCompanyName());
            templateParams.put("reportUrl", reportUrl);

            // 添加面试分数（如果有）
            if (interviewReport.getInterviewScore() != null) {
                templateParams.put("interviewScore", interviewReport.getInterviewScore());
            }

            // 添加链接有效期信息（如果是预签名URL）
            if (isPresignedUrl) {
                // 计算有效期（小时）
                int expireHours = reportUrlExpireSeconds / 3600;
                templateParams.put("linkExpireHours", expireHours);
                templateParams.put("isPresignedUrl", true);
            } else {
                templateParams.put("isPresignedUrl", false);
            }

            // 8. 根据职位地理位置选择模板和主题（因为回调时没有用户上下文）
            // 使用职位国家ID判断是否使用中文模板
            boolean chinese = isJobLocationInChina(candidateJobEntity);
            String templateName = chinese ? "Interview-report-zh.html" : "Interview-report.html";
            String subject = chinese
                    ? String.format("面试报告 - %s 的 %s 岗", companyInfo.getCompanyName(), jobEntity.getTitle())
                    : String.format("Interview Report - %s at %s", jobEntity.getTitle(), companyInfo.getCompanyName());

            // 9. 发送邮件
            mailUtils.sendHtmlTemplateMail(
                    "no-reply@item.com",
                    candidateEntity.getCandidateEmail(),
                    subject,
                    templateName,
                    templateParams
            );

            log.info("Interview report email sent successfully for candidateJobId: {}, email: {}",
                    candidateJobId, candidateEntity.getCandidateEmail());

        } catch (Exception e) {
            log.error("Failed to send interview report email for candidateJobId: {}", candidateJobId, e);
            throw new BusinessException(CandidateResponseCode.EMAIL_SEND_FAILURE);
        }
    }

    private void checkInterviewTime(CandidateJobEntity candidateJob) throws BusinessException {
        if (candidateJob != null && Objects.equals(InterviewPhoneStatusEnum.SCHEDULED.getCode(), candidateJob.getInterviewPhoneStatus())) {
            throw new BusinessException(GlobalStatusCode.AI_PHONE_CALL_FAILED, "A phone interview has been scheduled.");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String interviewTime = candidateJob.getPreferredInterviewStartTime();
        OffsetDateTime preferredInterviewStartTime = OffsetDateTime.parse(interviewTime, formatter);
        OffsetDateTime currentTimeUtc = OffsetDateTime.now(ZoneOffset.UTC);

        if (!currentTimeUtc.isBefore(preferredInterviewStartTime)) {
            log.error("Current time must be greater than PreferredInterviewStartTime. Current: {}, Preferred: {}",
                    currentTimeUtc, preferredInterviewStartTime);
            throw new BusinessException(GlobalStatusCode.AI_PHONE_CALL_FAILED,
                    "Current time must be before than candidate preferred interview time : " + interviewTime);
        }
    }

    private String getInterviewTime(CandidateJobEntity candidateJob) throws BusinessException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String interviewTime = candidateJob.getPreferredInterviewStartTime();
        OffsetDateTime preferredInterviewStartTime = OffsetDateTime.parse(interviewTime, formatter);
        OffsetDateTime currentTimeUtc = OffsetDateTime.now(ZoneOffset.UTC);

        if (currentTimeUtc.isBefore(preferredInterviewStartTime)) {
            return candidateJob.getPreferredInterviewStartTime();
        } else {
            log.error("Current time must be greater than PreferredInterviewStartTime. Current: {}, Preferred: {}",
                    currentTimeUtc, preferredInterviewStartTime);
            throw new BusinessException(GlobalStatusCode.AI_PHONE_CALL_FAILED,
                    "Current time must be before than candidate preferred interview time : " + interviewTime);
        }
    }

    @Override
    public Pager<CandidateJobRecordVO> getCandidateJobRecords(Long candidateId, int pageIndex, int pageSize) {
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        Pager<JobMatchResultVO> candidateJobRecords = resumeEsService.getCandidateJobRecords(candidateId, currentUserNeedLogin.getCompanyCode(), pageIndex, pageSize);
        List<JobMatchResultVO> currentPageRecords = candidateJobRecords.getCurrentPageRecords();
        if (CollectionUtils.isNotEmpty(currentPageRecords)) {
            List<Long> candidateIds = currentPageRecords.stream().map(JobMatchResultVO::getId).toList();
            List<CandidateJobEntity> candidateJobEntities = candidateJobService.listByIds(candidateIds);
            List<Long> countryIds=candidateJobEntities.stream().map(CandidateJobEntity::getJobCountryId).filter(Objects::nonNull).toList();
            List<Long> stateIds=candidateJobEntities.stream().map(CandidateJobEntity::getJobStateId).filter(Objects::nonNull).toList();
            List<Long> cityIds=candidateJobEntities.stream().map(CandidateJobEntity::getJobCityId).filter(Objects::nonNull).toList();

            Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(countryIds);
            Map<Long, String> stateMap =locationService.listIdNameMapByStateIds(stateIds);
            Map<Long, String> cityMap=locationService.listIdNameMapByCityIds(cityIds);
            Map<String, String> placeNameMap = getPlaceName(candidateJobEntities);

            List<CandidateJobRecordVO> candidateJobRecordVOs = currentPageRecords.stream()
                    .map(c->convertToCandidateJobRecordVO(c, candidateJobEntities, countryMap, stateMap, cityMap, placeNameMap))
                    .collect(Collectors.toList());

            Pager<CandidateJobRecordVO> pageResult = new Pager<>();
            pageResult.setCurrentPageRecords(candidateJobRecordVOs);
            pageResult.setPageIndex(pageIndex);
            pageResult.setPageSize(pageSize);
            pageResult.setTotalCount(candidateJobRecords.getTotalCount());
            return pageResult;
        } else {
            Pager<CandidateJobRecordVO> pageResult = new Pager<>();
            pageResult.setCurrentPageRecords(Collections.emptyList());
            pageResult.setPageIndex(pageIndex);
            pageResult.setPageSize(pageSize);
            pageResult.setTotalCount(0);
            return pageResult;
        }
    }

    @Override
    public Boolean isAnswerQuestion5sTest(AnswerQuestion5sTestStatusRequestDTO requestDTO) {
        String signature = requestDTO.getSignature();
        String candidateJobId = requestDTO.getCandidateJobId();
        String dateTime = requestDTO.getDateTime();

        Long candidateJobIdNum = checkValidAndGetId(signature, candidateJobId, dateTime);

        checkoutDateTimeValid(dateTime);

        CandidateJobEntity candidateJobInfo = candidateJobService.getCandidateJobById(candidateJobIdNum);
        if (candidateJobInfo == null) {
            throw BusinessException.of(CandidateResponseCode.NO_DELIVERY_RECORD_FOUND);
        }

        AnswerQuestion5SInfoDTO answerQuestion5S = parseQuestion5SInfo(candidateJobInfo);
        log.info("isAnswerQuestion5sTest {}", candidateJobInfo);
        if (answerQuestion5S == null) {
            return false;
        }
        return !StringUtils.isBlank(answerQuestion5S.getScoreFor5S());
    }

    @Override
    public Boolean answerQuestion5sTestConfirm(AnswerQuestion5sTestConfirmRequestDTO requestDTO) {
        String score = requestDTO.getScore();
        String signature = requestDTO.getSignature();
        String candidateJobId = requestDTO.getCandidateJobId();
        String dateTime = requestDTO.getDateTime();
        Long candidateJobIdNum = checkValidAndGetId(signature, candidateJobId, dateTime);
        checkoutDateTimeValid(dateTime);
        CandidateJobEntity candidateJobInfo = candidateJobService.getCandidateJobById(candidateJobIdNum);
        if (candidateJobInfo == null) {
            throw BusinessException.of(CandidateResponseCode.NO_DELIVERY_RECORD_FOUND);
        }
        CandidateAnswerQuestionInfoDTO candidateAnswerQuestionInfoDTO = parseQuestionInfo(candidateJobInfo);
        if (candidateAnswerQuestionInfoDTO != null &&
                candidateAnswerQuestionInfoDTO.getAnswerQuestion5S() != null &&
                StringUtils.isNotBlank(candidateAnswerQuestionInfoDTO.getAnswerQuestion5S().getScoreFor5S())) {
            throw BusinessException.of(CandidateResponseCode.NO_NEED_REPEAT_ANSWER);
        }
        candidateAnswerQuestionInfoDTO = fillCandidateAnswerQuestionInfoDTO(candidateAnswerQuestionInfoDTO, score);
        String json = JsonUtils.toJson(candidateAnswerQuestionInfoDTO);
        candidateJobService.updateCandidateQuestionInfoById(candidateJobIdNum, json);
        return true;
    }

    private void checkoutDateTimeValid(String dateTime) {
        // 校验链接有效期
        Integer validHours = recruitCommonNacosConfig.getAnswerQuestion5sLinkValidHours();
        if (validHours != null && validHours > 0) {
            try {
                // dateTime是UTC时间戳（秒级），格式如：1768815978
                long linkTimestamp = Long.parseLong(dateTime);
                // 将时间戳转换为OffsetDateTime（UTC时区）
                OffsetDateTime linkDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(linkTimestamp), ZoneOffset.UTC);
                OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);

                // 计算小时差
                long hoursDiff = ChronoUnit.HOURS.between(linkDateTime, currentDateTime);

                if (hoursDiff > validHours) {
                    log.warn("Answer question 5s link expired. Link time: {} (timestamp: {}), Current time: {}, Valid hours: {}, Hours diff: {}",
                            linkDateTime, linkTimestamp, currentDateTime, validHours, hoursDiff);
                    throw BusinessException.of(CandidateResponseCode.QUESTION_5S_TEST_LINK_EXPIRED);
                }
            } catch (Exception e) {
                log.error("Failed to parse timestamp. DateTime: {}", dateTime, e);
                // 如果解析失败，为了安全起见，抛出异常
                throw BusinessException.of(CandidateResponseCode.QUESTION_5S_TEST_INVALID_LINK);
            }
        }
    }

    private Long checkValidAndGetId(String signature, String candidateJobId, String dateTime) {
        if (StringUtils.isBlank(signature) || StringUtils.isBlank(candidateJobId) || StringUtils.isBlank(dateTime)) {
            throw BusinessException.of(CandidateResponseCode.QUESTION_5S_TEST_LINK_INVALID);
        }
        String join = CommonUtils.join(candidateJobId, dateTime);
        String signatureInput = Md5SignatureUtil.md5(join);
        if (!signature.equals(signatureInput)) {
            throw BusinessException.of(CandidateResponseCode.QUESTION_5S_TEST_LINK_INVALID);
        }
        Long candidateJobIdNum = shortIdGenerator.decrypt(candidateJobId, QUESTION_5S);
        if (candidateJobIdNum == null || candidateJobIdNum <= 0) {
            throw BusinessException.of(CandidateResponseCode.QUESTION_5S_TEST_LINK_INVALID);
        }
        return candidateJobIdNum;
    }

    private CandidateAnswerQuestionInfoDTO parseQuestionInfo(CandidateJobEntity candidateJobInfo) {
        if (candidateJobInfo == null) {
            return null;
        }
        String questionInfo = candidateJobInfo.getQuestionInfo();
        if (StringUtils.isBlank(questionInfo)) {
            return null;
        }
        try {
            return JsonUtils.toObject(questionInfo, CandidateAnswerQuestionInfoDTO.class);
        } catch (Exception e) {
            log.error("parseQuestionInfo fail", e);
            return null;
        }
    }

    private AnswerQuestion5SInfoDTO parseQuestion5SInfo(CandidateJobEntity candidateJobInfo) {
        CandidateAnswerQuestionInfoDTO candidateAnswerQuestionInfoDTO = parseQuestionInfo(candidateJobInfo);
        log.info("parseQuestion5SInfo {}", candidateAnswerQuestionInfoDTO);
        if (candidateAnswerQuestionInfoDTO == null) {
            return null;
        }
        return candidateAnswerQuestionInfoDTO.getAnswerQuestion5S();
    }

    private CandidateAnswerQuestionInfoDTO fillCandidateAnswerQuestionInfoDTO(CandidateAnswerQuestionInfoDTO questionInfoDTO, String question5SScore) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String nowStr = CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER.format(now);
        AnswerQuestion5SInfoDTO build = AnswerQuestion5SInfoDTO.builder().scoreFor5S(question5SScore).answerConfirmDateTime(nowStr).build();
        if (questionInfoDTO ==null){
            return CandidateAnswerQuestionInfoDTO.builder().answerQuestion5S(build).build();
        }
        questionInfoDTO.setAnswerQuestion5S(build);
        return questionInfoDTO;
    }

    /**
     * 将JobMatchResultVO转换为CandidateJobRecordVO
     *
     * @param jobMatchResultVO JobMatchResultVO对象
     * @return CandidateJobRecordVO对象
     */
    private CandidateJobRecordVO convertToCandidateJobRecordVO(JobMatchResultVO jobMatchResultVO,List<CandidateJobEntity> candidateJobEntities,
                                                               Map<Long, String> countryMap, Map<Long, String> stateMap,Map<Long, String> cityMap, Map<String, String> placeNameMap) {
        CandidateJobRecordVO candidateJobRecordVO = new CandidateJobRecordVO();
        // 转换字段
        candidateJobRecordVO.setId(jobMatchResultVO.getId());
        candidateJobRecordVO.setCandidateId(jobMatchResultVO.getCandidateId());
        candidateJobRecordVO.setJobId(jobMatchResultVO.getJobId());
        candidateJobRecordVO.setCandidateName(jobMatchResultVO.getCandidateName());
        candidateJobRecordVO.setJobTitle(jobMatchResultVO.getTitle());
        candidateJobRecordVO.setApplyStatus(jobMatchResultVO.getApplyStatus());
        candidateJobRecordVO.setApplyStatusName(jobMatchResultVO.getApplyStatusName());
        candidateJobRecordVO.setCreateTime(jobMatchResultVO.getCreateTime());

        Optional<CandidateJobEntity> candidateJobEntityOptional = candidateJobEntities.stream().filter(c -> c.getId().equals(jobMatchResultVO.getId())).findFirst();
        if (candidateJobEntityOptional.isPresent()){
            candidateJobRecordVO.setCountryName(countryMap.get(candidateJobEntityOptional.get().getJobCountryId()));
            candidateJobRecordVO.setStateName(stateMap.get(candidateJobEntityOptional.get().getJobStateId()));
            candidateJobRecordVO.setCityName(cityMap.get(candidateJobEntityOptional.get().getJobCityId()));
            candidateJobRecordVO.setPlaceName(placeNameMap.get(candidateJobEntityOptional.get().getPlaceId()));
        }
        return candidateJobRecordVO;
    }

    private Map<String, String> getPlaceName(List<CandidateJobEntity> candidateJobEntities) {
        if (CollectionUtils.isEmpty(candidateJobEntities)) {
            return new HashMap<>();
        }

        List<String> placeIds = candidateJobEntities.stream()
                .map(CandidateJobEntity::getPlaceId)
                .filter(placeId -> placeId != null && !placeId.isEmpty())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(placeIds)) {
            return new HashMap<>();
        }

        return locationService.listIdNameMapByPlaceIds(placeIds);
    }
}
