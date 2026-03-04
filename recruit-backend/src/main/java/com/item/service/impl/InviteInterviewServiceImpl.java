package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.convert.CandidateConverter;
import com.item.convert.CandidateEducationConverter;
import com.item.convert.EmploymentHistoryConverter;
import com.item.convert.InviteInterviewConvert;
import com.item.dto.CandidateDTO;
import com.item.dto.CandidateEducationDTO;
import com.item.dto.CityDTO;
import com.item.dto.CountryDTO;
import com.item.dto.EmploymentHistoryDTO;
import com.item.dto.StateDTO;
import com.item.entity.CandidateEducationEntity;
import com.item.entity.EmploymentHistoryEntity;
import com.item.dto.iam.IamCreateUserReqDTO;
import com.item.dto.iam.IamCreateUserResDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateEsEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.es.ResumeEsService;
import com.item.framework.config.IamCommonConfig;
import com.item.framework.constant.ApplyMethodEnum;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.InterviewMailStatusEnum;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.framework.constant.JobResponseCode;
import com.item.framework.constant.JobStatus;
import com.item.framework.error.BusinessException;
import com.item.service.CandidateJobDomainService;
import com.item.service.CandidateJobService;
import com.item.service.CandidateService;
import com.item.service.InviteInterviewService;
import com.item.service.JobService;
import com.item.service.JobStatusRecordService;
import com.item.service.LocationService;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.util.LanguageLocalUtils;
import com.item.util.UserContextUtil;
import com.item.vo.InviteInterviewParsedResumeVO;
import com.item.vo.InviteInterviewRequestVO;
import com.item.vo.InviteInterviewResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.item.framework.constant.JobStatus.ACTIVE;

/**
 * 邀请面试服务实现类
 * 实现HR邀请外部候选人面试的核心业务逻辑
 *
 * @since 2025-11-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteInterviewServiceImpl implements InviteInterviewService {
    
    private final JobService jobService;
    private final CandidateService candidateService;
    private final CandidateJobDomainService candidateJobDomainService;
    private final CandidateJobService candidateJobService;
    private final IamRpcAdapter iamRpcAdapter;
    private final IamCommonConfig iamCommonConfig;
    private final JobStatusRecordService jobStatusRecordService;
    private final LocationService locationService;
    private final ResumeEsService resumeEsService;
    private final CandidateConverter candidateConverter;
    private final ThreadPoolTaskExecutor aiTaskExecutor;

    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InviteInterviewResponseVO inviteInterview(InviteInterviewRequestVO request) {
        log.info("Invite interview request: jobId={}, candidateEmail={}, candidateName={}, hasParsedResume={}", 
                request.getJobId(), request.getParsedResume().getEmail(), request.getCandidateName(),
                request.getParsedResume() != null);
        
        try {
            String candidateName = Stream.of(request.getParsedResume().getFirstName(), request.getParsedResume().getLastName()) // 按顺序拼接
                    .filter(s -> s != null && !s.isBlank())    // 过滤掉 null 或空字符串
                    .collect(Collectors.joining(" "));
            request.setCandidateName(candidateName);
            // 获取并验证职位信息
            JobEntity jobEntity = getAndValidateJob(request.getJobId());
            
            // 获取简历解析结果（由前端调用"/resume-parsing"接口后回传）
            InviteInterviewParsedResumeVO parsedResume = request.getParsedResume();
            if (parsedResume != null) {
                log.info("Using parsed resume data: email={}, firstName={}, lastName={}, phoneNumber={}", 
                        parsedResume.getEmail(), parsedResume.getFirstName(), 
                        parsedResume.getLastName(), parsedResume.getPhoneNumber());
            }
            
            // 执行邀请面试
            return doInviteInterview(request, jobEntity, parsedResume);
            
        } catch (BusinessException e) {
            log.warn("Invite interview failed with business exception: jobId={}, email={}, error={}", 
                    request.getJobId(), request.getParsedResume().getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Invite interview failed with unexpected error: jobId={}, email={}", 
                    request.getJobId(), request.getParsedResume().getEmail(), e);
            String errorMessage = LanguageLocalUtils.getErrorMessage(
                    "Failed to invite interview", 
                    "邀请面试失败"
            );
            throw new BusinessException(GlobalStatusCode.FAIL, errorMessage);
        }
    }
    
    /**
     * 执行邀请面试的核心业务逻辑
     */
    private InviteInterviewResponseVO doInviteInterview(
            InviteInterviewRequestVO request, 
            JobEntity jobEntity, 
            InviteInterviewParsedResumeVO parsedResume) {
        addCandidateLocationName(request);
        parsedResume.setCountryId(request.getJobCountryId());
        parsedResume.setCityId(request.getJobCityId());
        parsedResume.setStateId(request.getJobStateId());
        if (Objects.nonNull(jobEntity.getSalaryType())) {
            parsedResume.setSalaryTypeId(Long.valueOf(jobEntity.getSalaryType()));
        }
        if (Objects.nonNull(jobEntity.getCurrency())) {
            parsedResume.setCurrencyTypeId(Long.valueOf(jobEntity.getCurrency()));
        }
        if (Objects.nonNull(jobEntity.getMaxSalary()) && Objects.nonNull(jobEntity.getMinSalary())) {
            parsedResume.setExpectedSalary((jobEntity.getMaxSalary()+jobEntity.getMinSalary())/2);
        } else if (Objects.nonNull(jobEntity.getMaxSalary())) {
            parsedResume.setExpectedSalary(jobEntity.getMaxSalary());
        } else if (Objects.nonNull(jobEntity.getMinSalary())) {
            parsedResume.setExpectedSalary(jobEntity.getMinSalary());
        }
        // 1. 获取或创建候选人
        CandidateEntity candidateEntity = getOrCreateCandidate(request, parsedResume);
        
        // 2. 保存候选人信息到ES（recruit_resume_ext索引）
        saveCandidateToEs(candidateEntity, parsedResume);
        
        // 3. 检查是否已存在申请记录
        checkExistingApplication(candidateEntity.getId(), jobEntity.getId());
        
        // 4. 创建职位申请记录
        CandidateJobEntity candidateJobEntity = buildCandidateJobEntity(candidateEntity, jobEntity, request);
        LocalDateTime dateTime = LocalDateTime.now();
        candidateJobEntity.setUpdateTime(dateTime);
        
        // 添加职位地址名称（如果缺失，根据ID查询补充）
        addJobLocationName(candidateJobEntity);
        
        // 保存申请记录
        candidateJobService.applyJob(candidateJobEntity);
        log.info("Candidate job application created: applicationId={}, candidateId={}, jobId={}", 
                candidateJobEntity.getId(), candidateEntity.getId(), request.getJobId());
        
        // 保存流转记录
        saveJobStatusRecord(candidateJobEntity, dateTime);
        
        // 5. 异步触发简历匹配流程
        triggerResumeAIMatch(candidateJobEntity, jobEntity);
        
        // 6. 返回响应
        return InviteInterviewConvert.INSTANCE.toResponseVO(
                candidateJobEntity.getId(),
                candidateEntity.getId(),
                "Interview invitation created successfully. Resume matching is in progress."
        );
    }
    
    /**
     * 获取并验证职位信息
     */
    private JobEntity getAndValidateJob(Long jobId) {
        JobEntity jobEntity = jobService.getJobsByIds(jobId);
        if (jobEntity == null) {
            log.warn("Job not found: jobId={}", jobId);
            String errorMessage = LanguageLocalUtils.getJobResponseCodeMessage(JobResponseCode.JOB_NOT_FOUND);
            throw new BusinessException(JobResponseCode.JOB_NOT_FOUND.getCode(), errorMessage);
        }
        
        // 验证职位状态
        if (JobStatus.getByCode(jobEntity.getJobStatus()) != ACTIVE) {
            log.warn("Job status is not active: jobId={}, status={}", jobId, jobEntity.getJobStatus());
            String errorMessage = LanguageLocalUtils.getJobResponseCodeMessage(JobResponseCode.JOB_STATUS_NOT_ACTIVE);
            throw new BusinessException(JobResponseCode.JOB_STATUS_NOT_ACTIVE.getCode(), errorMessage);
        }
        
        // 验证职位companyCode
        if (StringUtils.isBlank(jobEntity.getCompanyCode())) {
            log.warn("Job company code is blank: jobId={}", jobId);
            String errorMessage = LanguageLocalUtils.getJobResponseCodeMessage(JobResponseCode.JOB_NOT_FOUND);
            throw new BusinessException(JobResponseCode.JOB_NOT_FOUND.getCode(), errorMessage);
        }
        
        return jobEntity;
    }
    
    /**
     * 获取或创建候选人
     * 如果候选人已存在，根据传入的应聘者信息更新数据库
     */
    private CandidateEntity getOrCreateCandidate(InviteInterviewRequestVO request, InviteInterviewParsedResumeVO parsedResume) {
        LambdaQueryWrapper<CandidateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateEntity::getCandidatePermanentEmail, parsedResume.getEmail()).last("LIMIT 1");
        // 根据邮箱查询候选人
        CandidateEntity candidateEntity = candidateService.getOne(queryWrapper);
        
        if (candidateEntity == null) {
            log.info("Candidate not found, creating new candidate: email={}", parsedResume.getEmail());
            candidateEntity = createCandidateWithIamAccount(request, parsedResume);
        } else {
            log.info("Candidate already exists: candidateId={}, email={}", candidateEntity.getId(), parsedResume.getEmail());
            // 如果候选人已存在但IAM账号不存在，需要创建IAM账号
            if (candidateEntity.getCandidateId() == null) {
                log.info("Candidate exists but IAM account not found, creating IAM account: email={}", parsedResume.getEmail());
                createIamAccountForExistingCandidate(candidateEntity);
            }
            // 如果提供了简历解析结果，根据传入的应聘者信息更新数据库
            if (parsedResume != null) {
                log.info("Updating existing candidate with parsed resume data: candidateId={}", candidateEntity.getId());
                updateCandidateFromParsedResume(candidateEntity, request, parsedResume);
            }
        }
        
        return candidateEntity;
    }
    
    /**
     * 根据传入的应聘者信息更新数据库
     */
    private void updateCandidateFromParsedResume(
            CandidateEntity candidateEntity, 
            InviteInterviewRequestVO request, 
            InviteInterviewParsedResumeVO parsedResume) {
        
        try {
            // 1. 将parsedResume转换为CandidateDTO
            CandidateDTO candidateDTO = candidateConverter.convertInviteInterviewParsedResumeVOToDto(parsedResume);
            candidateDTO.setId(candidateEntity.getId());
            
            // 2. 使用转换器更新CandidateEntity（保留现有字段，只更新新字段）
            candidateConverter.dtoToEntity(candidateDTO, candidateEntity);
            
            // 3. 更新基本字段（确保使用正确的值）
            candidateEntity.setCandidateName(request.getCandidateName());
            candidateEntity.setUpdateTime(LocalDateTime.now());
            
            // 4. 如果有简历解析结果，设置uploadStatus为1
            if (StringUtils.isNotEmpty(candidateEntity.getResumeUrl())) {
                candidateEntity.setUploadStatus(1);
            }
            // 5. 更新数据库中的候选人信息
            candidateService.updateById(candidateEntity);
            log.info("Candidate updated in database: candidateId={}", candidateEntity.getId());
            
        } catch (Exception e) {
            log.error("Failed to update candidate from parsed resume: candidateId={}", candidateEntity.getId(), e);
            // 更新失败不影响主流程，只记录日志
            // 如果需要，可以在这里添加重试逻辑或告警
        }
    }
    
    /**
     * 检查是否已存在申请记录
     */
    private void checkExistingApplication(Long candidateId, Long jobId) {
        List<CandidateJobEntity> existingApplications = candidateJobService.getByCandidateIdAndJobId(candidateId, jobId);
        if (CollectionUtils.isNotEmpty(existingApplications)) {
            log.warn("Application already exists: candidateId={}, jobId={}, applicationId={}", 
                    candidateId, jobId, existingApplications.get(0).getId());
            // 如果已存在申请记录，可以选择抛出异常或返回现有记录
            // 这里选择抛出异常，因为这是"邀请"操作，不应该重复邀请
            String errorMessage = LanguageLocalUtils.getErrorMessage(
                    "Application already exists for this candidate and job", 
                    "该候选人和职位的申请记录已存在"
            );
            throw new BusinessException(GlobalStatusCode.FAIL, errorMessage);
        }
    }
    
    /**
     * 构建职位申请实体
     * 地址信息从请求参数中获取
     */
    private CandidateJobEntity buildCandidateJobEntity(
            CandidateEntity candidateEntity, 
            JobEntity jobEntity,
            InviteInterviewRequestVO request) {
        
        CandidateJobEntity candidateJobEntity = new CandidateJobEntity();
        candidateJobEntity.setCandidateId(candidateEntity.getId());
        candidateJobEntity.setJobId(jobEntity.getId());
        candidateJobEntity.setCompanyCode(jobEntity.getCompanyCode());
        candidateJobEntity.setCustomerId(jobEntity.getCustomerId());
        candidateJobEntity.setPosted(0);
        candidateJobEntity.setApplyStatus(JobApplyStatus.SUBMITTED.getCode());
        candidateJobEntity.setInterviewMailStatus(InterviewMailStatusEnum.NOTSEND.getCode());
        candidateJobEntity.setReason("");
        // 设置申请方式为邀请面试自动投递
        candidateJobEntity.setApplyMethod(ApplyMethodEnum.INVITE_INTERVIEW_AUTO_APPLY.getCode());
        
        // 设置职位地址信息（从请求参数中获取）
        candidateJobEntity.setJobCountryId(request.getJobCountryId());
        candidateJobEntity.setJobStateId(request.getJobStateId());
        candidateJobEntity.setJobCityId(request.getJobCityId());
        candidateJobEntity.setJobCountryName(request.getJobCountryName());
        candidateJobEntity.setJobStateName(request.getJobStateName());
        candidateJobEntity.setJobCityName(request.getJobCityName());
        candidateJobEntity.setPlaceId(request.getPlaceId());
        
        return candidateJobEntity;
    }
    
    /**
     * 添加职位地址名称（如果缺失）
     * 复用CandidateJobDomainServiceImpl中的逻辑
     */
    private void addJobLocationName(CandidateJobEntity candidateJobEntity) {
        if (!org.springframework.util.StringUtils.hasText(candidateJobEntity.getJobCountryName()) 
                && candidateJobEntity.getJobCountryId() != null) {
            List<com.item.dto.CountryDTO> countryDTOS = locationService.listByCountryIds(
                    List.of(candidateJobEntity.getJobCountryId()));
            if (CollectionUtils.isNotEmpty(countryDTOS)) {
                candidateJobEntity.setJobCountryName(countryDTOS.getFirst().getName());
            }
        }
        if (!org.springframework.util.StringUtils.hasText(candidateJobEntity.getJobStateName()) 
                && candidateJobEntity.getJobStateId() != null) {
            List<com.item.dto.StateDTO> stateDTOS = locationService.listByStateIds(
                    List.of(candidateJobEntity.getJobStateId()));
            if (CollectionUtils.isNotEmpty(stateDTOS)) {
                candidateJobEntity.setJobStateName(stateDTOS.getFirst().getName());
            }
        }
        if (!org.springframework.util.StringUtils.hasText(candidateJobEntity.getJobCityName()) 
                && candidateJobEntity.getJobCityId() != null) {
            List<com.item.dto.CityDTO> cityDTOS = locationService.listByCityIds(
                    List.of(candidateJobEntity.getJobCityId()));
            if (CollectionUtils.isNotEmpty(cityDTOS)) {
                candidateJobEntity.setJobCityName(cityDTOS.getFirst().getName());
            }
        }
    }
    
    /**
     * 保存职位状态流转记录
     */
    private void saveJobStatusRecord(CandidateJobEntity candidateJobEntity, LocalDateTime dateTime) {
        jobStatusRecordService.saveJobStatusRecord(
                candidateJobEntity.getId(), 
                candidateJobEntity.getCompanyCode(), 
                null, 
                candidateJobEntity.getApplyStatus(), 
                JobApplyStatusEvent.SUBMIT.toString(), 
                dateTime
        );
    }
    
    /**
     * 保存候选人信息到ES（recruit_resume_ext索引）
     * 参考CandidateServiceImpl中的实现模式
     */
    private void saveCandidateToEs(CandidateEntity candidateEntity, InviteInterviewParsedResumeVO parsedResume) {
        try {
            log.info("Saving candidate to ES: candidateId={}, email={}, hasParsedResume={}", 
                    candidateEntity.getId(), candidateEntity.getCandidateEmail(), parsedResume != null);
            
            // 提取教育经历（如果有）
            List<CandidateEducationEntity> educationEntities = new ArrayList<>();
            if (parsedResume != null && CollectionUtils.isNotEmpty(parsedResume.getEducationList())) {
                educationEntities = parsedResume.getEducationList().stream()
                        .map(vo -> {
                            CandidateEducationDTO dto = CandidateEducationConverter.INSTANCE
                                    .convertInviteInterviewEducationVOToDto(vo);
                            CandidateEducationEntity entity = CandidateEducationConverter.INSTANCE
                                    .convertDtoToEntity(dto);
                            entity.setCandidateId(candidateEntity.getId());
                            return entity;
                        })
                        .collect(Collectors.toList());
            }
            
            // 提取工作经历（如果有）
            List<EmploymentHistoryEntity> employmentEntities = new ArrayList<>();
            if (parsedResume != null && CollectionUtils.isNotEmpty(parsedResume.getEmploymentList())) {
                employmentEntities = parsedResume.getEmploymentList().stream()
                        .map(vo -> {
                            EmploymentHistoryDTO dto = EmploymentHistoryConverter.INSTANCE
                                    .convertInviteInterviewEmploymentHistoryVOToDto(vo);
                            EmploymentHistoryEntity entity = EmploymentHistoryConverter.INSTANCE
                                    .convertDtoToEntity(dto);
                            entity.setCandidateId(candidateEntity.getId());
                            return entity;
                        })
                        .collect(Collectors.toList());
            }
            
            // 构建ES实体（参考CandidateServiceImpl的实现模式）
            CandidateEsEntity candidateEsEntity = CandidateEsEntity.builder()
                    .id(candidateEntity.getId())
                    .candidateEducations(educationEntities)
                    .employmentHistories(employmentEntities)
                    .build();
            
            // 如果有简历解析结果，设置uploadStatus为1
            if (parsedResume != null) {
                candidateEntity.setUploadStatus(1);
            }
            
            // 使用转换器将CandidateEntity转换为CandidateEsEntity（填充其他字段）
            candidateConverter.entityToEsEntity(candidateEntity, candidateEsEntity);
            
            // 保存到ES
            resumeEsService.saveResumeToEs(candidateEsEntity);
            
            log.info("Candidate saved to ES successfully: candidateId={}, educationCount={}, employmentCount={}", 
                    candidateEntity.getId(), educationEntities.size(), employmentEntities.size());
        } catch (Exception e) {
            log.error("Failed to save candidate to ES: candidateId={}", candidateEntity.getId(), e);
            // ES保存失败不影响主流程，只记录日志
            // 如果需要，可以在这里添加重试逻辑或告警
        }
    }
    
    /**
     * 异步触发简历匹配流程
     */
    private void triggerResumeAIMatch(CandidateJobEntity candidateJobEntity, JobEntity jobEntity) {
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();
        Long userId = currentUser != null ? Long.parseLong(currentUser.getId()) : jobEntity.getCreateBy();
        String companyCode = jobEntity.getCompanyCode();
        
        aiTaskExecutor.execute(() -> {
            try {
                log.info("Starting async resume AI match: applicationId={}", candidateJobEntity.getId());
                candidateJobDomainService.resumeAIMatch(candidateJobEntity, userId, companyCode);
                log.info("Resume AI match completed: applicationId={}", candidateJobEntity.getId());
            } catch (Exception e) {
                log.error("Resume AI match error: applicationId={}", candidateJobEntity.getId(), e);
            }
        });
    }
    
    /**
     * 创建候选人并创建IAM账号
     */
    private CandidateEntity createCandidateWithIamAccount(
            InviteInterviewRequestVO request, 
            InviteInterviewParsedResumeVO parsedResume) {
        // 创建候选人记录
        CandidateEntity candidate = new CandidateEntity();

        // 构建IAM创建用户请求
        IamCreateUserReqDTO iamReq = new IamCreateUserReqDTO();

        // 直接从parsedResume获取firstName、lastName和phoneNumber
        String firstName = parsedResume.getFirstName();
        String lastName = parsedResume.getLastName();
        String phoneNumber = parsedResume.getPhoneNumber();

        iamReq.setEmail(parsedResume.getEmail());
        // 用户名使用邮箱
        iamReq.setUserName(parsedResume.getEmail());
        iamReq.setFirstName(firstName);
        iamReq.setLastName(lastName);
        iamReq.setContactNumber(phoneNumber);

        // 获取IAM配置
        IamCommonConfig.RegisterCandidateConfig config = iamCommonConfig.getRegisterCandidate();

        // 使用Nacos配置的默认密码
        String defaultPassword = config.getDefaultPassword();
        iamReq.setRawPassword(defaultPassword);

        iamReq.setCompanyCode(config.getBelong2CompanyCode());
        iamReq.setGrantedAppCodes(config.getGrantedAppCodes());

        log.info("Creating IAM account: email={}, companyCode={}", parsedResume.getEmail(), config.getBelong2CompanyCode());

        // 调用IAM创建用户
        IamCreateUserResDTO iamRes;
        try {
            iamRes = iamRpcAdapter.createUser(iamReq);
            candidate.setPassword(defaultPassword);
            candidate.setIsPasswordTold(0);
        } catch (BusinessException e) {
            if (e.getCode() == 210010022) {
                // 用户已存在，根据邮箱获取IAM用户信息
                log.info("IAM user already exists, getting user info by email: {}", parsedResume.getEmail());
                iamRes = iamRpcAdapter.getUserByEmail(parsedResume.getEmail());
                if (iamRes == null) {
                    log.error("Failed to get IAM user by email: {}", parsedResume.getEmail());
                    String errorMessage = LanguageLocalUtils.getErrorMessage(
                            "IAM user already exists but failed to get user info", 
                            "IAM用户已存在，但获取用户信息失败"
                    );
                    throw new BusinessException(GlobalStatusCode.FAIL, errorMessage);
                }
                log.info("IAM user info retrieved successfully: iamId={}", iamRes.getId());
            } else {
                log.error("Failed to create IAM user: {}", e.getMessage(), e);
                throw e;
            }
        }

        log.info("IAM account created successfully: iamId={}", iamRes.getId());


        // 如果有简历解析结果，使用转换器填充字段
        if (parsedResume != null) {
            CandidateDTO candidateDTO = candidateConverter.convertInviteInterviewParsedResumeVOToDto(parsedResume);
            // 使用转换器将DTO的属性复制到Entity
            candidateConverter.dtoToEntity(candidateDTO, candidate);
        }
        
        // 设置基本信息和IAM相关字段（这些字段会覆盖转换器设置的值，确保使用正确的值）
        candidate.setCandidateName(request.getCandidateName());
        candidate.setCandidateId(Long.parseLong(iamRes.getId()));
        if (StringUtils.isNotEmpty(candidate.getResumeUrl())) {
            candidate.setUploadStatus(1);
        }
        candidateService.save(candidate);
        log.info("Candidate created successfully: candidateId={}, iamId={}, isPasswordTold={}", 
                candidate.getId(), iamRes.getId(), candidate.getIsPasswordTold());
        
        return candidate;
    }
    
    /**
     * 为已存在的候选人创建IAM账号
     */
    private void createIamAccountForExistingCandidate(CandidateEntity candidateEntity) {
        
        // 构建IAM创建用户请求
        IamCreateUserReqDTO iamReq = new IamCreateUserReqDTO();
        iamReq.setEmail(candidateEntity.getCandidateEmail());
        // 用户名使用邮箱
        iamReq.setUserName(candidateEntity.getCandidateEmail());
        iamReq.setFirstName(candidateEntity.getFirstName());
        iamReq.setLastName(candidateEntity.getLastName());
        iamReq.setContactNumber(candidateEntity.getPhoneNumber());
        
        // 获取IAM配置
        IamCommonConfig.RegisterCandidateConfig config = iamCommonConfig.getRegisterCandidate();
        
        // 使用Nacos配置的默认密码
        String defaultPassword = config.getDefaultPassword();
        iamReq.setRawPassword(defaultPassword);
        
        iamReq.setCompanyCode(config.getBelong2CompanyCode());
        iamReq.setGrantedAppCodes(config.getGrantedAppCodes());
        
        log.info("Creating IAM account for existing candidate: email={}", candidateEntity.getCandidateEmail());
        
        // 调用IAM创建用户
        IamCreateUserResDTO iamRes;
        try {
            iamRes = iamRpcAdapter.createUser(iamReq);
            candidateEntity.setPassword(defaultPassword);
            candidateEntity.setIsPasswordTold(0);
        } catch (BusinessException e) {
            if (e.getCode() == 210010022) {
                // 用户已存在，根据邮箱获取IAM用户信息
                log.info("IAM user already exists, getting user info by email: {}", candidateEntity.getCandidateEmail());
                iamRes = iamRpcAdapter.getUserByEmail(candidateEntity.getCandidateEmail());
                if (iamRes == null) {
                    log.error("Failed to get IAM user by email: {}", candidateEntity.getCandidateEmail());
                    String errorMessage = LanguageLocalUtils.getErrorMessage(
                            "IAM user already exists but failed to get user info", 
                            "IAM用户已存在，但获取用户信息失败"
                    );
                    throw new BusinessException(GlobalStatusCode.FAIL, errorMessage);
                }
                log.info("IAM user info retrieved successfully: iamId={}", iamRes.getId());
            } else {
                log.error("Failed to create IAM user: {}", e.getMessage(), e);
                throw e;
            }
        }
        
        // 更新候选人的IAM ID
        candidateEntity.setCandidateId(Long.parseLong(iamRes.getId()));
        candidateService.updateById(candidateEntity);
        log.info("IAM account created/retrieved and candidate updated: candidateId={}, iamId={}, isPasswordTold={}", 
                candidateEntity.getId(), iamRes.getId(), candidateEntity.getIsPasswordTold());
    }

    /**
     * 添加候选人投递岗位地址名称
     * @param request
     */
    public void addCandidateLocationName(InviteInterviewRequestVO request){
        if (!org.springframework.util.StringUtils.hasText(request.getJobCountryName())){
            List<CountryDTO> countryDTOS = locationService.listByCountryIds(List.of(request.getJobCountryId()));
            if (CollectionUtils.isNotEmpty(countryDTOS)){
                request.setJobCountryName(countryDTOS.getFirst().getName());
            }
        }
        if (!org.springframework.util.StringUtils.hasText(request.getJobStateName())){
            List<StateDTO> stateDTOS = locationService.listByStateIds(List.of(request.getJobStateId()));
            if (CollectionUtils.isNotEmpty(stateDTOS)){
                request.setJobStateName(stateDTOS.getFirst().getName());
            }
        }
        if (!org.springframework.util.StringUtils.hasText(request.getJobCityName())){
            List<CityDTO> cityDTOS = locationService.listByCityIds(List.of(request.getJobCityId()));
            if (CollectionUtils.isNotEmpty(cityDTOS)){
                request.setJobCityName(cityDTOS.getFirst().getName());
            }
        }
    }
    
}

