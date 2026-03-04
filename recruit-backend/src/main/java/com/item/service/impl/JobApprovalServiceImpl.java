package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.convert.JobConvert;
import com.item.dto.DictionaryDTO;
import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.iam.IamUserDTO;
import com.item.dto.job.JobAuditHistoryBO;
import com.item.dto.job.JobCreateBO;
import com.item.dto.job.JobUpdateBO;
import com.item.dto.job.LocationValRecordDTO;
import com.item.entity.*;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.constant.*;
import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.mapper.JobApprovalSettingsMapper;
import com.item.service.*;
import com.item.service.client.IamUserClient;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.util.UserContextUtil;
import com.item.vo.JobApprovalRequestVO;
import com.item.vo.JobApprovalSettingsVO;
import com.item.vo.JobAuditHistoryVO;
import com.item.vo.PendingJobListVO;
import com.item.vo.ai.InterviewResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.item.framework.constant.JobResponseCode.JOB_SALARY_TYPE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobApprovalServiceImpl implements JobApprovalService {

    private final JobApprovalSettingsMapper jobApprovalSettingsMapper;
    private final ThreadPoolTaskExecutor aiTaskExecutor;
    private final XmlFeedConfigService xmlFeedConfigService;
    private final NaukriService naukriService;
    private final IamRpcAdapter iamRpcAdapter;
    private final JobPostProcessingService jobPostProcessingService;
    private final JobApprovalEmailRetryService emailRetryService;
    private final JobAuditHistoryService jobAuditHistoryService;
    private final JobDoubleDataSourceService jobDoubleDataSourceService;
    private final JobEsService jobEsService;
    private final JobService jobService;
    private final DictionaryService dictionaryService;
    private final IamUserClient iamUserClient;
    private final LocationService locationService;

    @Override
    public Boolean submitJobForApproval(Long jobId) {
        JobEsEntity jobEsEntity = jobEsService.getJobById(jobId);
        if (jobEsEntity == null) {
            return false;
        }

        JobEntity jobEntity = jobService.getJobsByIds(jobId);
        if (jobEntity == null) {
            return false;
        }

        String currentUserCompanyCode = UserContextUtil.getCurrentUserCompanyCode();
        if (!currentUserCompanyCode.equals(jobEntity.getCompanyCode())) {
            log.warn("User {} attempted to submit job {} from different company {}",
                    UserContextUtil.getCurrentUserNeedLogin().getId(), jobId, jobEntity.getCompanyCode());
            return false;
        }

        IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(currentUserCompanyCode);
        var jobUpdateBO = enrichJobUpdateBO(jobEsEntity, jobEntity, companyInfo, JobStatus.PENDING_REVIEW.getCode());
        JobAuditHistoryBO audit = new JobAuditHistoryBO();
        audit.setJobId(jobUpdateBO.getJobId());
        audit.setOldStatus(jobEntity.getJobStatus());
        audit.setNewStatus(jobUpdateBO.getJobStatus());
        audit.setAction(JobApprovalAction.SUBMITTED.toString());
        audit.setComment(null);
        audit.setCreatedBy(Long.valueOf(UserContextUtil.getCurrentUserNeedLogin().getId()));
        audit.setCreatedAt(LocalDateTime.now());
        jobUpdateBO.setJobAuditHistory(audit);
        var updateJob = jobDoubleDataSourceService.updateJob(jobUpdateBO);

        if (!updateJob) {
            log.warn("update job failed {}", jobEntity);
            throw BusinessException.of(JobResponseCode.JOB_UPDATE_FAIL);
        }

        String adminEmail = iamUserClient.getAdminEmailByCompanyCode(jobEntity.getCompanyCode());
        sendApprovalNotification(jobId, JobApprovalAction.SUBMITTED.toString(), null, adminEmail);

        return true;
    }

    private JobUpdateBO enrichJobUpdateBO(JobEsEntity jobEsEntity, JobEntity jobEntity, IamCompanyDetailDTO companyInfo, Integer jobStatus) {
        JobUpdateBO jobUpdateBO = JobConvert.INSTANCE.toJobUpdateBoFromEntity(jobEsEntity);
        jobUpdateBO.setJobStatus(jobStatus);
        jobUpdateBO.setSubmittedForApprovalAt(LocalDateTime.now());
        jobUpdateBO.setApprovedAt(jobEntity.getApprovedAt());
        jobUpdateBO.setApprovedBy(jobEntity.getApprovedBy());
        jobUpdateBO.setDeniedAt(jobEntity.getDeniedAt());
        jobUpdateBO.setDeniedBy(jobEntity.getDeniedBy());
        jobUpdateBO.setInterviewUrlId(jobEntity.getInterviewUrlId());
        jobUpdateBO.setUpdateUser(UserContextUtil.getCurrentUserNeedLogin().getUserName());
        jobUpdateBO.setUpdateBy(Long.parseLong(UserContextUtil.getCurrentUserNeedLogin().getId()));
        jobUpdateBO.setModeName(jobEntity.getJobMode() != null ? jobEntity.getJobMode().getModeName() : null);
        jobUpdateBO.setTypeName(jobEntity.getJobType() != null ? jobEntity.getJobType().getName() : null);
        jobUpdateBO.setCategoryName(jobEntity.getJobCategory() != null ? jobEntity.getJobCategory().getName() : null);
        jobUpdateBO.setCompanyCode(jobEntity.getCompanyCode());
        jobUpdateBO.setCustomerName(companyInfo.getCompanyName());
        jobUpdateBO.setLogoPath(companyInfo.getLogopath());
        jobUpdateBO.setCurrencyName(getCurrencyName(jobEntity.getCurrency()));
        jobUpdateBO.setSalaryTypeName(getSalaryTypeName(jobEntity.getSalaryType()));
        jobUpdateBO.setUrlCode(jobEntity.getUrlCode());
        jobUpdateBO.setCompanyTitleHash(jobEntity.getCompanyTitleHash());
        return jobUpdateBO;
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

    @Override
    public Boolean processJobApproval(JobApprovalRequestVO request) {
        JobEsEntity jobEsEntity = jobEsService.getJobById(request.getJobId());
        if (jobEsEntity == null) {
            return false;
        }

        JobEntity jobEntity = jobService.getJobsByIds(request.getJobId());
        if (jobEntity == null) {
            return false;
        }

        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();

        if (!currentUserNeedLogin.getCompanyCode().equalsIgnoreCase(jobEntity.getCompanyCode())) {
            log.warn("User {} attempted to process job {} from different company {}",
                    currentUserNeedLogin.getId(), request.getJobId(), jobEntity.getCompanyCode());
            return false;
        }

        Long currentUserId = Long.valueOf(currentUserNeedLogin.getId());
        LocalDateTime now = LocalDateTime.now();
        Integer oldStatus = jobEntity.getJobStatus();

        // Boundary case validation according to PRD
        if (request.getAction() == JobApprovalAction.REJECT) {
            if (request.getComment() == null || request.getComment().trim().isEmpty()) {
                log.warn("Reject operation requires a comment for job {}", request.getJobId());
                throw new BusinessException(GlobalStatusCode.PARAM_ERROR,
                        String.format("Please fill in the reason for rejecting the position (%d-%d characters)",
                                JobApprovalConstants.MIN_COMMENT_LENGTH, JobApprovalConstants.MAX_COMMENT_LENGTH));
            }
            if (request.getComment().trim().length() < JobApprovalConstants.MIN_COMMENT_LENGTH ||
                    request.getComment().trim().length() > JobApprovalConstants.MAX_COMMENT_LENGTH) {
                log.warn("Comment length invalid for job {}: {}", request.getJobId(), request.getComment().length());
                throw new BusinessException(GlobalStatusCode.PARAM_ERROR,
                        String.format("Comment must be between %d-%d characters",
                                JobApprovalConstants.MIN_COMMENT_LENGTH, JobApprovalConstants.MAX_COMMENT_LENGTH));
            }
        }

        Integer jobStatus;
        switch (request.getAction()) {
            case APPROVE:
                // According to PRD: Pending Review → Approve → Pending Publication
                jobStatus = JobStatus.PENDING_PUBLICATION.getCode();
                jobEntity.setApprovedAt(now);
                jobEntity.setApprovedBy(currentUserId);
                break;
            case REJECT:
                // According to PRD: Pending Review → Reject → Pending Modification
                jobStatus = JobStatus.PENDING_MODIFICATION.getCode();
                jobEntity.setDeniedAt(now);
                jobEntity.setDeniedBy(currentUserId);
                break;
            default:
                return false;
        }

        IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(currentUserNeedLogin.getCompanyCode());

        var jobUpdateBo = enrichJobUpdateBO(jobEsEntity, jobEntity, companyInfo, jobStatus);
        String actionComment = request.getComment();
        JobAuditHistoryBO audit = new JobAuditHistoryBO();
        audit.setJobId(jobUpdateBo.getJobId());
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(jobStatus);
        audit.setAction(request.getAction().toString());
        audit.setComment(actionComment);
        audit.setCreatedBy(Long.valueOf(UserContextUtil.getCurrentUserNeedLogin().getId()));
        audit.setCreatedAt(LocalDateTime.now());
        jobUpdateBo.setJobAuditHistory(audit);
        var updateJob = jobDoubleDataSourceService.updateJob(jobUpdateBo);

        if (!updateJob) {
            log.warn("update job failed {}. The current job is in the {} state and cannot be edited", jobEntity, oldStatus);
            throw new BusinessException(10000,
                    String.format(JobResponseCode.JOB_UPDATE_FAIL + ": The current job is in the %d state and cannot be edited", oldStatus));
        }

        // After main account approves or rejects, notify only the creator (sub-account)

        String creatorEmail = iamUserClient.getUserEmailById(jobEntity.getCreateBy());
        sendApprovalNotification(jobEntity.getId(), request.getAction().toString(), actionComment, creatorEmail);
        return true;
    }

    @Override
    public JobApprovalSettingsVO getApprovalSettings(String companyCode) {
        LambdaQueryWrapper<JobApprovalSettingsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApprovalSettingsEntity::getCompanyCode, companyCode);

        JobApprovalSettingsEntity entity = jobApprovalSettingsMapper.selectOne(wrapper);

        JobApprovalSettingsVO settings = new JobApprovalSettingsVO();
        settings.setCompanyCode(companyCode);

        if (entity != null) {
            settings.setApprovalRequired(entity.getApprovalRequired());
            settings.setEmailNotifications(entity.getEmailNotifications());
            settings.setAdminEmail(entity.getAdminEmail());
        } else {
            // return approval not required if none exist
            settings.setEmailNotifications(false);
            settings.setApprovalRequired(false);
        }

        return settings;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createApprovalSettings(JobApprovalSettingsVO settings) {
        LambdaQueryWrapper<JobApprovalSettingsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApprovalSettingsEntity::getCompanyCode, settings.getCompanyCode());

        JobApprovalSettingsEntity existingEntity = jobApprovalSettingsMapper.selectOne(wrapper);

        if (existingEntity != null) {
            log.warn("Approval settings already exist for company: {}", settings.getCompanyCode());
            return false;
        }

        JobApprovalSettingsEntity newEntity = new JobApprovalSettingsEntity();
        newEntity.setCompanyCode(settings.getCompanyCode());
        newEntity.setApprovalRequired(settings.getApprovalRequired());
        newEntity.setEmailNotifications(settings.getEmailNotifications());
        newEntity.setAdminEmail(settings.getAdminEmail());
        newEntity.setCreateTime(LocalDateTime.now());
        newEntity.setUpdateTime(LocalDateTime.now());
        newEntity.setCreateById(Long.valueOf(UserContextUtil.getCurrentUserNeedLogin().getId()));
        newEntity.setUpdateById(Long.valueOf(UserContextUtil.getCurrentUserNeedLogin().getId()));

        return jobApprovalSettingsMapper.insert(newEntity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateApprovalSettings(JobApprovalSettingsVO settings) {
        LambdaQueryWrapper<JobApprovalSettingsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApprovalSettingsEntity::getCompanyCode, settings.getCompanyCode());

        JobApprovalSettingsEntity existingEntity = jobApprovalSettingsMapper.selectOne(wrapper);

        if (existingEntity == null) {
            log.warn("No approval settings found for company: {}", settings.getCompanyCode());
            return false;
        }

        existingEntity.setApprovalRequired(settings.getApprovalRequired());
        existingEntity.setEmailNotifications(settings.getEmailNotifications());
        existingEntity.setAdminEmail(settings.getAdminEmail());
        existingEntity.setUpdateTime(LocalDateTime.now());
        existingEntity.setUpdateById(Long.valueOf(UserContextUtil.getCurrentUserNeedLogin().getId()));

        return jobApprovalSettingsMapper.updateById(existingEntity) > 0;
    }

    @Override
    public Pager<PendingJobListVO> getPendingApprovalJobs(String companyCode, int pageNo, int pageSize) {
        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobEntity::getCompanyCode, companyCode)
                .in(JobEntity::getJobStatus, JobStatus.PENDING_REVIEW.getCode())
                .orderByDesc(JobEntity::getSubmittedForApprovalAt);

        // Use MyBatis Plus pagination
        Page<JobEntity> page = new Page<>(pageNo, pageSize);
        Page<JobEntity> jobPage = jobService.page(page, wrapper);

        // Convert JobEntity to PendingJobListVO
        List<PendingJobListVO> pendingJobVOs = jobPage.getRecords().stream()
                .map(job -> {
                    PendingJobListVO vo = new PendingJobListVO();
                    vo.setJobId(job.getId());
                    vo.setTitle(job.getTitle());
                    vo.setJobStatus(job.getJobStatus());
                    vo.setCreateTime(job.getCreateTime());
                    vo.setCreateUser(job.getCreateUser());
                    vo.setLocationName(job.getLocationName());
                    vo.setMinSalary(job.getMinSalary());
                    vo.setMaxSalary(job.getMaxSalary());
                    vo.setCompanyCode(job.getCompanyCode());
                    vo.setSubmittedForApprovalAt(job.getSubmittedForApprovalAt());
                    vo.setNumberOpenings(job.getNumberOpenings());

                    // Get the latest approval comment from audit history
                    LambdaQueryWrapper<JobAuditHistoryEntity> auditWrapper = new LambdaQueryWrapper<>();
                    auditWrapper.eq(JobAuditHistoryEntity::getJobId, job.getId())
                            .in(JobAuditHistoryEntity::getAction, "APPROVE", "REJECT", "MODIFY")
                            .orderByDesc(JobAuditHistoryEntity::getCreatedAt)
                            .last("LIMIT 1");
                    JobAuditHistoryEntity latestAudit = jobAuditHistoryService.getOne(auditWrapper);
                    if (latestAudit != null && latestAudit.getComment() != null) {
                        vo.setComment(latestAudit.getComment());
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        // Use Pager.build() method for proper pagination
        return Pager.build(jobPage, records -> pendingJobVOs);
    }

    @Override
    public Pager<PendingJobListVO> getAwaitingJobs(String companyCode, int pageNo, int pageSize) {
        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobEntity::getCompanyCode, companyCode)
                .in(JobEntity::getJobStatus, JobStatus.PENDING_PUBLICATION.getCode(), JobStatus.PENDING_MODIFICATION.getCode())
                .orderByDesc(JobEntity::getUpdateTime);

        // Use MyBatis Plus pagination
        Page<JobEntity> page = new Page<>(pageNo, pageSize);
        Page<JobEntity> jobPage = jobService.page(page, wrapper);

        // Convert JobEntity to PendingJobListVO
        List<PendingJobListVO> pendingJobVOs = jobPage.getRecords().stream()
                .map(job -> {
                    PendingJobListVO vo = new PendingJobListVO();
                    vo.setJobId(job.getId());
                    vo.setTitle(job.getTitle());
                    vo.setJobStatus(job.getJobStatus());
                    vo.setCreateTime(job.getCreateTime());
                    vo.setCreateUser(job.getCreateUser());
                    vo.setLocationName(job.getLocationName());
                    vo.setMinSalary(job.getMinSalary());
                    vo.setMaxSalary(job.getMaxSalary());
                    vo.setCompanyCode(job.getCompanyCode());
                    vo.setSubmittedForApprovalAt(job.getSubmittedForApprovalAt());
                    vo.setNumberOpenings(job.getNumberOpenings());

                    // Get the latest approval comment from audit history
                    LambdaQueryWrapper<JobAuditHistoryEntity> auditWrapper = new LambdaQueryWrapper<>();
                    auditWrapper.eq(JobAuditHistoryEntity::getJobId, job.getId())
                            .in(JobAuditHistoryEntity::getAction, "APPROVE", "REJECT", "MODIFY")
                            .orderByDesc(JobAuditHistoryEntity::getCreatedAt)
                            .last("LIMIT 1");
                    JobAuditHistoryEntity latestAudit = jobAuditHistoryService.getOne(auditWrapper);
                    if (latestAudit != null && latestAudit.getComment() != null) {
                        vo.setComment(latestAudit.getComment());
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        // Use Pager.build() method for proper pagination
        return Pager.build(jobPage, records -> pendingJobVOs);
    }

    @Override
    public Boolean publishJob(Long jobId) {
        JobEsEntity job = jobEsService.getJobById(jobId);
        if (job == null) {
            log.warn("Job not found for publishing: {}", jobId);
            return false;
        }

        // Security check: Ensure user can only publish jobs from their own company
        String currentUserCompanyCode = UserContextUtil.getCurrentUserCompanyCode();
        if (!currentUserCompanyCode.equals(job.getCompanyCode())) {
            log.warn("User {} attempted to publish job {} from different company {}",
                    UserContextUtil.getCurrentUserNeedLogin().getId(), jobId, job.getCompanyCode());
            return false;
        }

        // Check if job is in PENDING_PUBLICATION status
        if (!Objects.equals(job.getJobStatus(), JobStatus.PENDING_PUBLICATION.getCode())) {
            log.warn("Cannot publish job {} with status {}", jobId, job.getJobStatus());
            return false;
        }

        // According to PRD: Pending Publication → Publish → Published (ACTIVE)
        Integer oldStatus = job.getJobStatus();
        JobAuditHistoryBO audit = new JobAuditHistoryBO();
        audit.setJobId(jobId);
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(JobStatus.ACTIVE.getCode());
        audit.setAction(JobApprovalAction.PUBLISH.toString());
        audit.setComment(null);
        audit.setCreatedBy(Long.valueOf(UserContextUtil.getCurrentUserNeedLogin().getId()));
        audit.setCreatedAt(LocalDateTime.now());
        var updateJob = jobDoubleDataSourceService.updateJobStatus(jobId, JobStatus.ACTIVE.getCode(), audit);

        if (!updateJob) {
            log.warn("update job failed with id {}", jobId);
            throw BusinessException.of(JobResponseCode.JOB_UPDATE_FAIL);
        }

        // Notify the creator that the job is now published
        try {
            JobEntity jobEntity = jobService.getJobsByIds(jobId);
            String creatorEmail = jobEntity != null ? iamUserClient.getUserEmailById(jobEntity.getCreateBy()) : null;
            sendApprovalNotification(jobId, JobApprovalAction.PUBLISH.toString(), null, creatorEmail);
        } catch (Exception e) {
            log.warn("Failed to send publish notification for jobId={}", jobId, e);
        }

        // Do post-processing (AyrShare, Naukri, AI Interview)
        JobCreateBO jobCreateBO = JobConvert.INSTANCE.toJobCreateBoFromEntity(job);
        IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(currentUserCompanyCode);

        jobPostProcessingService.ayrSharePost(jobCreateBO, companyInfo, jobId);

        // Naukri posting for India or Saudi Arabia
        try {
            if (naukriService.shouldPostToNaukri(jobCreateBO)) {
                naukriService.asyncPostJob(jobCreateBO, companyInfo.getCompanyName(), jobId);
            } else {
                log.info("Naukri post skipped by country or toggle for jobId={} locations={}", jobId, jobCreateBO.getLocations());
            }
        } catch (Exception ex) {
            log.warn("Naukri post invoke failed jobId={} error=", jobId, ex);
        }

        //TODO 增加面试时长 发送到ai服务
        //生成url id 异步处理，定时任务补偿
        aiTaskExecutor.execute(() -> {
            long start = System.currentTimeMillis();
            log.info("createAIInterview jobId:{}", jobId);
            InterviewResultVO interviewResultVO = jobPostProcessingService.createAIInterview(jobCreateBO);
            log.info("createAIInterview jobId:{},time:{},结果:{}", jobId, System.currentTimeMillis() - start, interviewResultVO);
            jobCreateBO.setInterviewUrlId(interviewResultVO.getUrlId());
            jobService.updateInterviewUrlId(jobId, jobCreateBO.getInterviewUrlId());
        });
        //异步生成 feed xml
        aiTaskExecutor.execute(() -> {
            xmlFeedConfigService.jobUpdate(currentUserCompanyCode, Long.valueOf(UserContextUtil.getCurrentUserNeedLogin().getId()));
        });

        log.info("Job {} successfully published and post-processing initiated", jobId);
        return true;
    }

    @Override
    public void sendApprovalNotification(Long jobId, String action, String comment, String email) {
        JobEntity job = jobService.getJobsByIds(jobId);
        if (job == null) {
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            log.warn("Approval notification email is empty for job {}", jobId);
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("jobTitle", job.getTitle());
        variables.put("jobId", jobId);
        variables.put("action", action);
        variables.put("comment", comment);
        variables.put("companyCode", job.getCompanyCode());

        // Determine language from first job location (default English)
        String language = "en";
        try {
            JobEsEntity jobEs = jobEsService.getJobById(jobId);
            if (jobEs != null && jobEs.getLocations() != null && !jobEs.getLocations().isEmpty()) {
                LocationValRecordDTO firstLocation = jobEs.getLocations().get(0);
                if (firstLocation != null && firstLocation.getCountryId() != null) {
                    var country = locationService.getCountryById(firstLocation.getCountryId());
                    if (country != null && country.getName() != null) {
                        // Simple rule: China -> Chinese, otherwise English
                        language = "China".equalsIgnoreCase(country.getName()) ? "zh" : "en";
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to determine language from job locations for jobId={}", jobId, e);
        }
        String templateName = getEmailTemplateName(action);
        if ("zh".equals(language)) {
            templateName = templateName + "-zh";
        }

        emailRetryService.sendEmailWithRetry(jobId, action, comment, templateName, variables, new String[]{email});
    }

    @Override
    public List<JobAuditHistoryVO> getJobAuditHistory(Long jobId) {
        JobEntity job = jobService.getJobsByIds(jobId);
        if (job == null) {
            log.warn("Job {} not found", jobId);
            return List.of();
        }

        String currentUserCompanyCode = UserContextUtil.getCurrentUserCompanyCode();
        if (!currentUserCompanyCode.equals(job.getCompanyCode())) {
            log.warn("User {} attempted to access audit history for job {} from different company {}",
                    UserContextUtil.getCurrentUserNeedLogin().getId(), jobId, job.getCompanyCode());
            return List.of();
        }

        LambdaQueryWrapper<JobAuditHistoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobAuditHistoryEntity::getJobId, jobId);
        wrapper.orderByDesc(JobAuditHistoryEntity::getCreatedAt);

        List<JobAuditHistoryEntity> entities = jobAuditHistoryService.list(wrapper);

        return entities.stream().map(entity -> {
            JobAuditHistoryVO vo = new JobAuditHistoryVO();
            vo.setId(entity.getId());
            vo.setAuditorId(String.valueOf(entity.getCreatedBy()));

            try {
                IamUserDTO user = iamRpcAdapter.getUserInfo(entity.getCreatedBy());
                vo.setAuditorName(user.getUserName());
            } catch (Exception e) {
                log.warn("Failed to get user info for {}", entity.getCreatedBy(), e);
                vo.setAuditorName("Unknown User");
            }

            vo.setFromStatus(entity.getOldStatus());
            vo.setToStatus(entity.getNewStatus());
            vo.setAuditTime(entity.getCreatedAt());
            vo.setComment(entity.getComment());
            vo.setAction(entity.getAction());

            return vo;
        }).collect(Collectors.toList());
    }

    private String getEmailTemplateName(String action) {
        return switch (action.toUpperCase()) {
            case "APPROVE" -> JobApprovalConstants.EMAIL_TEMPLATE_APPROVED;
            case "REJECT" -> JobApprovalConstants.EMAIL_TEMPLATE_DENIED;
            case "PUBLISH" -> JobApprovalConstants.EMAIL_TEMPLATE_PUBLISHED;
            default -> JobApprovalConstants.EMAIL_TEMPLATE_APPROVAL_REQUEST;
        };
    }

    private String getAdminEmailByCompanyCode(String companyCode) {
        try {
            var admin = iamRpcAdapter.getAdminByCompanyCode(companyCode);
            return admin != null ? admin.getEmail() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch admin by companyCode {}", companyCode, e);
            return null;
        }
    }

}
