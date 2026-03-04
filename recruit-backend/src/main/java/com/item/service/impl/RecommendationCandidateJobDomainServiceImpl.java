package com.item.service.impl;

import com.item.dto.CompanyInfoSimpleDTO;
import com.item.dto.SaveRecommendationDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.entity.CandidateEntity;
import com.item.entity.JobEntity;
import com.item.framework.constant.CandidateResponseCode;
import com.item.framework.constant.JobResponseCode;
import com.item.framework.error.BusinessException;
import com.item.service.CandidateService;
import com.item.service.CompanyService;
import com.item.service.GenerateUrlCodeService;
import com.item.service.JobDomainService;
import com.item.service.JobService;
import com.item.service.RecommendationCandidateJobDomainService;
import com.item.service.RecommendationCandidateJobService;
import com.item.service.ShortIdGenerator;
import com.item.util.LanguageLocalUtils;
import com.item.util.MailUtils;
import com.item.util.UserContextUtil;

import java.util.Locale;
import com.item.vo.RecommendJobEmailRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Recommendation candidate job domain service implementation
 * Implements business logic for sending job recommendation emails
 *
 * @author hua.liu
 * @since 2025-10-23
 */
@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class RecommendationCandidateJobDomainServiceImpl implements RecommendationCandidateJobDomainService {

    private final RecommendationCandidateJobService recommendationService;
    private final JobService jobService;
    private final CandidateService candidateService;
    private final MailUtils mailUtils;
    private final JobDomainService jobDomainService;
    private final CompanyService companyService;
    private final GenerateUrlCodeService generateUrlCodeService;
    private final ShortIdGenerator shortIdGenerator;

    @Value("${company.name.replace.str:,.，。#@%$}")
    private String companyNameReplace;

    @Override
    public Boolean sendRecommendationEmail(RecommendJobEmailRequestDTO request) {
        log.info("Starting recommendation email process: jobId={}, candidateId={}, recommendBy={}",
                request);
        // Get current recruiter ID from user context
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();
        Long recommendBy = Long.parseLong(currentUser.getId());
        Long jobId = request.getJobId();
        Long candidateId = request.getCandidateId();
        try {
            // 1. Check if already recommended
//            if (recommendationService.checkRecommendationExists(jobId, candidateId)) {
//                log.warn("Candidate {} has already been recommended for job {}", candidateId, jobId);
//                throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_APPLY_REPEATEDLY);
//            }

            // 2. Get job information
            JobEntity job = jobService.getById(jobId);
            if (job == null || !Strings.CS.equals(job.getCompanyCode(), currentUser.getCompanyCode())) {
                log.warn("Job not found: {} {}", job, currentUser);
                throw BusinessException.of(JobResponseCode.JOB_NOT_FOUND);
            }

            // 3. Get candidate information
            CandidateEntity candidate = candidateService.getById(candidateId);
            if (candidate == null) {
                log.error("Candidate not found: {}", candidateId);
                throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
            }

            // 4. Get candidate email
            String candidateEmail = candidate.getCandidateEmail();
            if (StringUtils.isBlank(candidateEmail)) {
                log.error("Candidate email is empty for candidateId: {} candidate {}", candidateId, candidate);
                throw BusinessException.of(CandidateResponseCode.CANDIDATE_CURRENT_PARAM_NOT_EQUALS);
            }

            // 5. Generate job share link
            String companyName = "";
            CompanyInfoSimpleDTO companyInfo = companyService.getCompanyInfoByCode(job.getCompanyCode());
            if (companyInfo != null) {
                companyName = companyInfo.getName();
            }
            String urlCode = generateUrlCodeService.generateUrlCodeByConfig(
                    job.getTitle(), job.getUrlCode(), companyName, companyNameReplace
            );
            String idStr = shortIdGenerator.generateShortId(job.getId());
            String shareLink = jobDomainService.generateInfoShareLink(idStr, urlCode);

            // 6. Build email template parameters
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("candidateName", candidate.getCandidateName());
            templateParams.put("jobTitle", job.getTitle());
            templateParams.put("companyName", companyName);
            templateParams.put("jobUrl", shareLink);
            log.info("jobUrl {}", shareLink);
            
            // 7. 根据语言环境选择模板和主题
            String templateName = LanguageLocalUtils.getEmailTemplateName("jobRecommendCandidateInvitation.html");
            Locale locale = UserContextUtil.getLanguageLocal();
            boolean chinese = LanguageLocalUtils.isChinese(locale);
            String subject = chinese 
                ? "职位推荐 - " + job.getTitle()
                : "Recommendation " + job.getTitle();
            
            // 8. Send email
            mailUtils.sendHtmlTemplateMail(
                    candidateEmail,
                    subject,
                    templateName,
                    templateParams
            );

            // 9. Save recommendation record
            SaveRecommendationDTO saveDTO = new SaveRecommendationDTO();
            saveDTO.setJobId(jobId);
            saveDTO.setCandidateId(candidateId);
            saveDTO.setRecommendBy(recommendBy);
            saveDTO.setCandidateEmail(candidateEmail);
            saveDTO.setRecommendReasons(request.getRecommendReasons());
            saveDTO.setRecommendTime(LocalDateTime.now());
            
            recommendationService.saveRecommendation(saveDTO);

            log.info("Successfully sent recommendation email for request {} saveDTO {}", request, saveDTO);
            return true;

        } catch (BusinessException e) {
            log.error("Business error when sending recommendation email: {}", request, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to send recommendation email: {}}", request, e);
            throw BusinessException.of(CandidateResponseCode.EMAIL_SEND_FAILURE);
        }
    }
}

