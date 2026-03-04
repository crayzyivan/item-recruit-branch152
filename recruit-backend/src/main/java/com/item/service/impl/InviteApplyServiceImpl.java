package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.convert.InviteApplyConvert;
import com.item.dto.iam.IamCreateUserReqDTO;
import com.item.dto.iam.IamCreateUserResDTO;
import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.entity.CandidateEntity;
import com.item.entity.JobEntity;
import com.item.framework.config.IamCommonConfig;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.JobResponseCode;
import com.item.framework.constant.JobStatus;
import com.item.framework.error.BusinessException;
import com.item.service.CandidateService;
import com.item.service.GenerateUrlCodeService;
import com.item.service.InviteApplyService;
import com.item.service.JobDomainService;
import com.item.service.JobService;
import com.item.service.ShortIdGenerator;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.util.LanguageLocalUtils;
import com.item.util.MailUtils;
import com.item.util.UserContextUtil;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import com.item.vo.InviteApplyRequestVO;
import com.item.vo.InviteApplyResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.item.framework.constant.JobStatus.ACTIVE;

/**
 * 邀请投递服务实现类
 * 实现HR邀请外部候选人投递职位的核心业务逻辑
 *
 * @since 2025-11-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteApplyServiceImpl implements InviteApplyService {

    private final JobService jobService;
    private final CandidateService candidateService;
    private final IamRpcAdapter iamRpcAdapter;
    private final IamCommonConfig iamCommonConfig;
    private final JobDomainService jobDomainService;
    private final MailUtils mailUtils;
    private final GenerateUrlCodeService generateUrlCodeService;
    private final ShortIdGenerator shortIdGenerator;

    @Value("${company.name.replace.str:,.，。#@%$}")
    private String companyNameReplace;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InviteApplyResponseVO inviteApply(InviteApplyRequestVO request) {
        log.info("Invite apply request: jobId={}, candidateEmail={}, candidateName={}",
                request.getJobId(), request.getCandidateEmail(), request.getCandidateName());

        try {
            // 1. 获取并验证职位信息（参数验证由Jakarta Validation注解在Controller层自动处理）
            JobEntity jobEntity = getAndValidateJob(request.getJobId());

            // 2. 获取或创建候选人，并判断IAM账号是否已存在
            boolean iamAccountExisted = getOrCreateCandidate(request);

            // 3. 获取公司名称
            String companyName = getCompanyName(jobEntity.getCompanyCode());

            // 4. 生成职位详情页链接
            String urlCode = generateUrlCodeService.generateUrlCodeByConfig(
                    jobEntity.getTitle(), jobEntity.getUrlCode(), companyName, companyNameReplace
            );
            String idStr = shortIdGenerator.generateShortId(jobEntity.getId());
            String jobUrl = jobDomainService.generateInfoShareLink(idStr, urlCode);
            log.info("Generated job URL: {}", jobUrl);

            // 5. 获取IAM账号密码（仅在新创建IAM账号时用于邮件）
            String password = null;
            if (!iamAccountExisted) {
                password = iamCommonConfig.getRegisterCandidate().getDefaultPassword();
            }

            // 6. 发送邀请投递邮件
            sendInviteApplyEmail(request, jobEntity, companyName, jobUrl, password, iamAccountExisted);

            // 7. 返回响应
            return InviteApplyConvert.INSTANCE.toResponseVO(
                    request.getCandidateEmail(),
                    "Invitation email sent successfully. Candidate can log in and apply for the job."
            );

        } catch (BusinessException e) {
            log.warn("Invite apply failed with business exception: jobId={}, email={}, error={}",
                    request.getJobId(), request.getCandidateEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Invite apply failed with unexpected error: jobId={}, email={}",
                    request.getJobId(), request.getCandidateEmail(), e);
            String errorMessage = LanguageLocalUtils.getErrorMessage(
                    "Failed to invite apply", 
                    "邀请投递失败"
            );
            throw new BusinessException(GlobalStatusCode.FAIL, errorMessage);
        }
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
     * 检查IAM账号是否已存在
     */
    private CandidateEntity checkIamAccountExisted(String email) {
        LambdaQueryWrapper<CandidateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateEntity::getCandidatePermanentEmail, email).last("LIMIT 1");
        return candidateService.getOne(queryWrapper);
    }

    /**
     * 获取或创建候选人
     */
    private boolean getOrCreateCandidate(InviteApplyRequestVO request) {
        // 根据邮箱查询候选人
        CandidateEntity candidateEntity = checkIamAccountExisted(request.getCandidateEmail());

        if (candidateEntity == null) {
            log.info("Candidate not found, creating new candidate: email={}", request.getCandidateEmail());
            return createCandidateWithIamAccount(request);
        } else {
            log.info("Candidate already exists: candidateId={}, email={}", candidateEntity.getId(), request.getCandidateEmail());
            // 如果候选人已存在但IAM账号不存在，需要创建IAM账号
            if (candidateEntity.getCandidateId() == null) {
                log.info("Candidate exists but IAM account not found, creating IAM account: email={}", request.getCandidateEmail());
                return createIamAccountForExistingCandidate(candidateEntity);
            }
        }
        return true;
    }

    /**
     * 创建候选人并创建IAM账号
     */
    private boolean createCandidateWithIamAccount(InviteApplyRequestVO request) {
        boolean iamAccountExisted = false;
                // 构建IAM创建用户请求
        IamCreateUserReqDTO iamReq = new IamCreateUserReqDTO();

        // 从candidateName拆分firstName和lastName
        String firstName;
        String lastName;
        String[] nameParts = request.getCandidateName().trim().split("\\s+", 2);
        if (nameParts.length >= 2) {
            firstName = nameParts[0];
            lastName = nameParts[1];
        } else {
            firstName = request.getCandidateName();
            lastName = "";
        }

        iamReq.setEmail(request.getCandidateEmail());
        // 用户名使用邮箱
        iamReq.setUserName(request.getCandidateEmail());
        iamReq.setFirstName(firstName);
        iamReq.setLastName(lastName);
        iamReq.setContactNumber(null);

        // 获取IAM配置
        IamCommonConfig.RegisterCandidateConfig config = iamCommonConfig.getRegisterCandidate();

        // 使用Nacos配置的默认密码
        String defaultPassword = config.getDefaultPassword();
        iamReq.setRawPassword(defaultPassword);

        iamReq.setCompanyCode(config.getBelong2CompanyCode());
        iamReq.setGrantedAppCodes(config.getGrantedAppCodes());

        log.info("Creating IAM account: email={}, companyCode={}", request.getCandidateEmail(), config.getBelong2CompanyCode());

        // 调用IAM创建用户
        IamCreateUserResDTO iamRes;
        try {
            iamRes = iamRpcAdapter.createUser(iamReq);
        } catch (BusinessException e) {
            if (e.getCode() == 210010022) {
                // 用户已存在，根据邮箱获取IAM用户信息
                log.info("IAM user already exists, getting user info by email: {}", request.getCandidateEmail());
                iamRes = iamRpcAdapter.getUserByEmail(request.getCandidateEmail());
                if (iamRes == null) {
                    log.error("Failed to get IAM user by email: {}", request.getCandidateEmail());
                    String errorMessage = LanguageLocalUtils.getErrorMessage(
                            "IAM user already exists but failed to get user info", 
                            "IAM用户已存在，但获取用户信息失败"
                    );
                    throw new BusinessException(GlobalStatusCode.FAIL, errorMessage);
                }
                iamAccountExisted = true;
                log.info("IAM user info retrieved successfully: iamId={}", iamRes.getId());
            } else {
                log.error("Failed to create IAM user: {}", e.getMessage(), e);
                throw e;
            }
        }

        log.info("IAM account created successfully: iamId={}", iamRes.getId());

        // 创建候选人记录
        CandidateEntity candidate = new CandidateEntity();
        candidate.setCandidateName(request.getCandidateName());
        candidate.setCandidateEmail(request.getCandidateEmail());
        candidate.setCandidatePermanentEmail(request.getCandidateEmail());
        candidate.setPhoneNumber(null);
        candidate.setCandidateId(Long.parseLong(iamRes.getId()));
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);

        candidateService.save(candidate);
        log.info("Candidate created successfully: candidateId={}, iamId={}", candidate.getId(), iamRes.getId());

        return iamAccountExisted;
    }

    /**
     * 为已存在的候选人创建IAM账号
     */
    private boolean createIamAccountForExistingCandidate(CandidateEntity candidateEntity) {
        boolean iamAccountExisted = false;
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
                iamAccountExisted = true;
                log.info("IAM user info retrieved successfully: iamId={}", iamRes.getId());
            } else {
                log.error("Failed to create IAM user: {}", e.getMessage(), e);
                throw e;
            }
        }
        
        // 更新候选人的IAM ID
        candidateEntity.setCandidateId(Long.parseLong(iamRes.getId()));
        candidateService.updateById(candidateEntity);
        log.info("IAM account created/retrieved and candidate updated: candidateId={}, iamId={}",
                candidateEntity.getId(), iamRes.getId());
        return iamAccountExisted;
    }

    /**
     * 获取公司名称
     */
    private String getCompanyName(String companyCode) {
        try {
            IamCompanyDetailDTO companyDetail = iamRpcAdapter.getCompanyDetailByCode(companyCode);
            if (companyDetail != null && StringUtils.isNotBlank(companyDetail.getCompanyName())) {
                return companyDetail.getCompanyName();
            }
        } catch (Exception e) {
            log.warn("Failed to get company name for companyCode: {}", companyCode, e);
        }
        return "Company";
    }

    /**
     * 发送邀请投递邮件
     * @param request 邀请投递请求
     * @param jobEntity 职位实体
     * @param companyName 公司名称
     * @param jobUrl 职位详情页链接
     * @param password 密码（如果IAM账号已存在则为null）
     * @param iamAccountExisted IAM账号是否已存在
     */
    private void sendInviteApplyEmail(InviteApplyRequestVO request, JobEntity jobEntity,
                                      String companyName, String jobUrl, String password, boolean iamAccountExisted) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("candidateName", request.getCandidateName());
            variables.put("jobTitle", jobEntity.getTitle());
            variables.put("companyName", companyName);
            variables.put("jobUrl", jobUrl);
            variables.put("iamAccount", request.getCandidateEmail());
            variables.put("showPassword", !iamAccountExisted); // 如果IAM账号已存在，则不显示密码
            if (!iamAccountExisted) {
                variables.put("password", password);
            }

            // 根据语言环境选择模板和主题
            String templateName = LanguageLocalUtils.getEmailTemplateName("InviteApplyInvitation.html");
            Locale locale = UserContextUtil.getLanguageLocal();
            boolean chinese = LanguageLocalUtils.isChinese(locale);
            String subject = chinese 
                ? "职位申请邀请 - " + jobEntity.getTitle()
                : "Job Application Invitation - " + jobEntity.getTitle();

            mailUtils.sendHtmlTemplateMail(
                    request.getCandidateEmail(),
                    subject,
                    templateName,
                    variables
            );

            log.info("Invite apply email sent successfully: email={}, jobId={}, iamAccountExisted={}", 
                    request.getCandidateEmail(), request.getJobId(), iamAccountExisted);
        } catch (Exception e) {
            log.error("Failed to send invite apply email: email={}, jobId={}", request.getCandidateEmail(), request.getJobId(), e);
            // 邮件发送失败不影响主流程，只记录日志
        }
    }
}

