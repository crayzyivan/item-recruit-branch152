package com.item.schedule;

import com.item.dto.CompanyInfoSimpleDTO;
import com.item.dto.CountryDTO;
import com.item.dto.ai.SendSmsRequestDTO;
import com.item.entity.*;
import com.item.framework.config.AiInterviewConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.framework.constant.AICallbackTypeEnum;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.framework.constant.RecallStatusEnum;
import com.item.framework.constant.SmsSendStatusEnum;
import com.item.framework.error.BusinessException;
import com.item.mapper.AICallbackMapper;
import com.item.service.*;
import com.item.task.core.handler.annotation.ScheduleTask;
import com.item.util.AiRestTemplateHeaderUtil;
import com.item.vo.ai.SendSmsResponseVO;
import com.item.task.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiPhoneInterviewRecallTask {

    private final AiInterviewConfig aiInterviewConfig;
    private final CandidateRecallService candidateRecallService;
    private final AIService aiService;
    private final CandidateJobService candidateJobService;
    private final JobService jobService;
    private final CandidateService candidateService;
    private final CompanyService companyService;
    private final LocationService locationService;
    private final RestTemplate restTemplate;
    private final JobFlowService jobFlowService;
    private final AICallbackMapper aiCallbackMapper;


    @ScheduleTask("aiPhoneInterviewRecallTask")
    public void aiPhoneInterviewRecallTask() {
        log.info("aiPhoneInterviewRecallTask start");
        XxlJobHelper.log("aiPhoneInterviewRecallTask start");

        List<String> errorMessages = new ArrayList<>();

        try {
            // 短信重发逻辑
            List<String> smsErrors = retrySendSms();
            errorMessages.addAll(smsErrors);

            // 处理超时未响应的候选人拒绝逻辑
            List<String> rejectErrors = rejectUnresponsiveCandidates();
            errorMessages.addAll(rejectErrors);
            
            // 重新呼叫面试
            List<String> recallErrors = recallCandidate();
            errorMessages.addAll(recallErrors);


            // 如果有错误，记录并标记失败
            if (!errorMessages.isEmpty()) {
                String errorSummary = String.join("; ", errorMessages);
                log.error("aiPhoneInterviewRecallTask completed with errors: {}", errorSummary);
                XxlJobHelper.log("aiPhoneInterviewRecallTask completed with errors: " + errorSummary);
                XxlJobHelper.handleFail(errorSummary);
            } else {
                log.info("aiPhoneInterviewRecallTask end successfully");
                XxlJobHelper.log("aiPhoneInterviewRecallTask end successfully");
            }
        } catch (Exception e) {
            String errorMsg = "aiPhoneInterviewRecallTask failed with exception: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log(errorMsg);
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    private List<String> recallCandidate() {
        List<String> errorMessages = new ArrayList<>();
        List<CandidateRecallEntity> recallList = candidateRecallService.listAllOrderByCreateTimeDesc();
        if (CollectionUtils.isEmpty(recallList)) {
            log.info("No recall records found");
            XxlJobHelper.log("No recall records found");
            return errorMessages;
        }

        // 1. 创建系统当前 UTC 时间
        LocalDateTime currentUtcTime = LocalDateTime.now(ZoneOffset.UTC);
        log.info("Current UTC time: {}", currentUtcTime);
        XxlJobHelper.log("Current UTC time: " + currentUtcTime);

        int successCount = 0;
        int failureCount = 0;

        for (CandidateRecallEntity recall : recallList) {
            try {
                // 2. 将 preferredInterviewStartTime 转换成 UTC 时间
                OffsetDateTime preferredStartTime = OffsetDateTime.parse(recall.getPreferredInterviewStartTime());
                LocalDateTime preferredStartTimeUtc = preferredStartTime.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

                log.info("Processing recall id: {}, candidateJobId: {}, preferredStartTime UTC: {}",
                        recall.getId(), recall.getCandidateJobId(), preferredStartTimeUtc);

                // 3. 判断当前 UTC 时间和 preferredInterviewStartTime 转换后的时间差是否大于等于 recallAfterHours
                long minutesDifference = ChronoUnit.MINUTES.between(preferredStartTimeUtc, currentUtcTime);
                log.info("Minutes difference: {}, recallAfterHours: {}", minutesDifference, aiInterviewConfig.getRecallAfterMinutes());

                if (minutesDifference >= aiInterviewConfig.getRecallAfterMinutes()) {
                    log.info("Time condition met, creating AI phone call for candidateJobId: {}", recall.getCandidateJobId());

                    // 4. 调用 createAiPhoneCall
                    createAiPhoneCall(recall.getScheduleId());

                    // 5. 成功调用后更新 recall 状态为已回拨
                    candidateRecallService.updateRecallStatusById(recall.getId(), RecallStatusEnum.RECALLED.getCode());
                    log.info("Successfully processed and updated recall record id: {} to RECALLED status", recall.getId());
                    successCount++;
                }
            } catch (Exception e) {
                failureCount++;
                String errorMsg = String.format("Error processing recall id: %s, candidateJobId: %s, error: %s",
                        recall.getId(), recall.getCandidateJobId(), e.getMessage());
                log.error(errorMsg, e);
                XxlJobHelper.log(errorMsg);
                errorMessages.add(errorMsg);
                // 继续处理下一个记录，不中断整个任务
            }
        }

        String summary = String.format("Recall candidate completed. Success: %d, Failure: %d", successCount, failureCount);
        log.info(summary);
        XxlJobHelper.log(summary);

        return errorMessages;
    }

    public void createAiPhoneCall(String scheduleId) {

        boolean success = aiService.recallAIPhoneInterview(scheduleId);
        if (!success) {
            log.error("Call bookingAiPhoneInterview result: {}", success);
            throw new BusinessException(GlobalStatusCode.AI_PHONE_CALL_FAILED, "ScheduleId:" + scheduleId);
        }
    }

    /**
     * 重发短信给未成功接收短信的候选人
     */
    private List<String> retrySendSms() {
        List<String> errorMessages = new ArrayList<>();
        // 1. 查询 smsStatus = 0 的记录
        List<CandidateRecallEntity> failedSmsList = candidateRecallService.listBySmsStatusFail();
        
        if (CollectionUtils.isEmpty(failedSmsList)) {
            log.info("No failed SMS records found");
            XxlJobHelper.log("No failed SMS records found");
            return errorMessages;
        }
        
        log.info("Found {} failed SMS records to retry", failedSmsList.size());
        XxlJobHelper.log("Found " + failedSmsList.size() + " failed SMS records to retry");
        
        int successCount = 0;
        int failureCount = 0;
        
        // 2. 遍历记录并发送短信
        for (CandidateRecallEntity recall : failedSmsList) {
            try {
                Long candidateJobId = recall.getCandidateJobId();
                String candidatePhone = recall.getInterviewPhone();
                
                // 验证必要字段
                if (candidateJobId == null || !StringUtils.hasText(candidatePhone)) {
                    String errorMsg = String.format("Invalid recall record, id: %s, candidateJobId: %s, phone: %s",
                            recall.getId(), candidateJobId, candidatePhone);
                    log.warn(errorMsg);
                    XxlJobHelper.log(errorMsg);
                    failureCount++;
                    errorMessages.add(errorMsg);
                    continue;
                }
                
                // 获取候选人职位信息
                CandidateJobEntity candidateJobEntity = candidateJobService.getById(candidateJobId);
                if (candidateJobEntity == null) {
                    String errorMsg = String.format("CandidateJob not found, candidateJobId: %s", candidateJobId);
                    log.warn(errorMsg);
                    XxlJobHelper.log(errorMsg);
                    failureCount++;
                    errorMessages.add(errorMsg);
                    continue;
                }
                
                // 获取职位信息
                JobEntity jobEntity = jobService.getById(candidateJobEntity.getJobId());
                if (jobEntity == null) {
                    String errorMsg = String.format("Job not found, jobId: %s", candidateJobEntity.getJobId());
                    log.warn(errorMsg);
                    XxlJobHelper.log(errorMsg);
                    failureCount++;
                    errorMessages.add(errorMsg);
                    continue;
                }
                
                // 获取候选人信息
                CandidateEntity candidateEntity = candidateService.getById(candidateJobEntity.getCandidateId());
                if (candidateEntity == null) {
                    String errorMsg = String.format("Candidate not found, candidateId: %s", candidateJobEntity.getCandidateId());
                    log.warn(errorMsg);
                    XxlJobHelper.log(errorMsg);
                    failureCount++;
                    errorMessages.add(errorMsg);
                    continue;
                }
                
                // 获取公司名称
                String companyName = "";
                if (StringUtils.hasText(candidateJobEntity.getCompanyCode())) {
                    CompanyInfoSimpleDTO companyInfo = companyService.getCompanyInfoByCode(candidateJobEntity.getCompanyCode());
                    if (companyInfo != null && StringUtils.hasText(companyInfo.getName())) {
                        companyName = companyInfo.getName();
                    }
                }
                
                String candidateName = candidateEntity.getCandidateName();
                String jobTitle = jobEntity.getTitle();
                
                // 判断语言（基于职位所在国家）
                boolean isChinese = isJobLocationInChina(candidateJobEntity);
                
                // 构建短信内容
                String recallPhoneNumber = aiInterviewConfig.getSendRecallPhone().get(recall.getInterviewLanguage());
                String smsBody = buildSmsMessage(isChinese, candidateName, companyName, jobTitle, 
                        recallPhoneNumber);
                
                // 发送短信
                SendSmsRequestDTO smsRequest = SendSmsRequestDTO.builder()
                        .from(aiInterviewConfig.getSendSmsPhone())
                        .to(candidatePhone)
                        .body(smsBody)
                        .build();
                
                HttpHeaders headers = AiRestTemplateHeaderUtil.addUserContextHeadersByJob(jobEntity);
                HttpEntity<SendSmsRequestDTO> requestEntity = new HttpEntity<>(smsRequest, headers);
                
                log.info("Retrying SMS to candidate, phone: {}, candidateJobId: {}", candidatePhone, candidateJobId);
                ResponseEntity<SendSmsResponseVO> response = restTemplate.postForEntity(
                        aiInterviewConfig.getSendSmsUrl(),
                        requestEntity,
                        SendSmsResponseVO.class
                );
                
                // 4. 判断是否成功，成功则更新状态
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    SendSmsResponseVO smsResponse = response.getBody();
                    if (Boolean.TRUE.equals(smsResponse.getSuccess())) {
                        log.info("SMS retry successful, messageSid: {}, to: {}", 
                                smsResponse.getMessageSid(), smsResponse.getTo());
                        candidateRecallService.updateSmsStatusByCandidateJobId(candidateJobId, 
                                SmsSendStatusEnum.SUCCESS.getCode());
                        successCount++;
                    } else {
                        String errorMsg = String.format("SMS retry failed, status: %s, to: %s",
                                smsResponse.getStatus(), smsResponse.getTo());
                        log.warn(errorMsg);
                        XxlJobHelper.log(errorMsg);
                        failureCount++;
                        errorMessages.add(errorMsg);
                    }
                } else {
                    String errorMsg = String.format("SMS retry failed, status code: %s", response.getStatusCode());
                    log.warn(errorMsg);
                    XxlJobHelper.log(errorMsg);
                    failureCount++;
                    errorMessages.add(errorMsg);
                }
                
            } catch (Exception e) {
                failureCount++;
                String errorMsg = String.format("Error retrying SMS for recall id: %s, candidateJobId: %s, error: %s",
                        recall.getId(), recall.getCandidateJobId(), e.getMessage());
                log.error(errorMsg, e);
                XxlJobHelper.log(errorMsg);
                errorMessages.add(errorMsg);
                // 继续处理下一个记录
            }
        }

        String summary = String.format("Retry SMS completed. Success: %d, Failure: %d", successCount, failureCount);
        log.info(summary);
        XxlJobHelper.log(summary);

        return errorMessages;
    }

    /**
     * 拒绝超时未响应的候选人
     */
    private List<String> rejectUnresponsiveCandidates() {
        List<String> errorMessages = new ArrayList<>();
        
        // 1. 查询 sms_status = 1 且 recall_status = 1 的记录
        List<CandidateRecallEntity> recalledList = candidateRecallService.listBySmsStatusAndRecallStatus(
                SmsSendStatusEnum.SUCCESS.getCode(), 
                RecallStatusEnum.RECALLED.getCode());
        
        if (CollectionUtils.isEmpty(recalledList)) {
            log.info("No recalled candidates found for rejection check");
            XxlJobHelper.log("No recalled candidates found for rejection check");
            return errorMessages;
        }
        
        log.info("Found {} recalled candidates to check for rejection", recalledList.size());
        XxlJobHelper.log("Found " + recalledList.size() + " recalled candidates to check for rejection");
        
        // 创建系统当前 UTC 时间
        LocalDateTime currentUtcTime = LocalDateTime.now(ZoneOffset.UTC);
        
        // 计算超时阈值（recallAfterMinutes + 5小时）
        long timeoutMinutes = aiInterviewConfig.getRecallAfterMinutes() + aiInterviewConfig.getRejectAfterMinutes();
        
        int successCount = 0;
        int failureCount = 0;
        
        for (CandidateRecallEntity recall : recalledList) {
            try {
                Long candidateJobId = recall.getCandidateJobId();
                
                if (candidateJobId == null) {
                    String errorMsg = String.format("Invalid recall record, id: %s, candidateJobId is null", recall.getId());
                    log.warn(errorMsg);
                    XxlJobHelper.log(errorMsg);
                    failureCount++;
                    errorMessages.add(errorMsg);
                    continue;
                }
                
                // 2. 查询 r_ai_callback 表中该 candidateJobId 的 PHONE_INTERVIEW_NOTE_CONNECTED 记录数
                LambdaQueryWrapper<AiCallbackEntity> callbackQueryWrapper = new LambdaQueryWrapper<>();
                callbackQueryWrapper.eq(AiCallbackEntity::getCandidateJobId, candidateJobId)
                        .eq(AiCallbackEntity::getEvent, AICallbackTypeEnum.PHONE_INTERVIEW_NOTE_CONNECTED.getName());
                Long notConnectedCount = aiCallbackMapper.selectCount(callbackQueryWrapper);
                
                log.info("CandidateJobId: {} has {} PHONE_INTERVIEW_NOTE_CONNECTED records", 
                        candidateJobId, notConnectedCount);
                
                // 3. 如果记录数少于 2 条，跳过该候选人
                if (notConnectedCount < 2) {
                    log.info("CandidateJobId: {} has less than 2 not connected records, skipping rejection", 
                            candidateJobId);

                    callbackQueryWrapper = new LambdaQueryWrapper<>();
                    callbackQueryWrapper.eq(AiCallbackEntity::getCandidateJobId, candidateJobId)
                            .eq(AiCallbackEntity::getEvent, AICallbackTypeEnum.PHONE_INTERVIEW_CANCELLED.getName());
                    Long canceledCount = aiCallbackMapper.selectCount(callbackQueryWrapper);
                    if (canceledCount < 1) {
                        continue;
                    }

                }
                
                // 4. 将 preferredInterviewStartTime 转换成 UTC 时间
                OffsetDateTime preferredStartTime = OffsetDateTime.parse(recall.getPreferredInterviewStartTime());
                LocalDateTime preferredStartTimeUtc = preferredStartTime.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                
                log.info("Checking rejection for recall id: {}, candidateJobId: {}, preferredStartTime UTC: {}",
                        recall.getId(), candidateJobId, preferredStartTimeUtc);
                
                // 5. 判断当前 UTC 时间和 preferredInterviewStartTime 转换后的时间差是否大于等于 timeoutMinutes
                long minutesDifference = ChronoUnit.MINUTES.between(preferredStartTimeUtc, currentUtcTime);
                log.info("Minutes difference: {}, timeout threshold: {}", minutesDifference, timeoutMinutes);
                
                if (minutesDifference >= timeoutMinutes) {
                    log.info("Timeout condition met, rejecting candidateJobId: {}", candidateJobId);
                    
                    // 6. 调用拒绝处理
                    jobFlowService.fireEvent(candidateJobId, JobApplyStatusEvent.REJECT);
                    
                    log.info("Successfully rejected candidateJobId: {} for recall id: {}", candidateJobId, recall.getId());
                    successCount++;
                    candidateRecallService.deleteByCandidateJobId(recall.getCandidateJobId());
                    log.info("Delete rejected recall record candidateJobId: {} for recall id: {}", candidateJobId, recall.getId());
                }

            } catch (Exception e) {
                failureCount++;
                String errorMsg = String.format("Error rejecting candidate for recall id: %s, candidateJobId: %s, error: %s",
                        recall.getId(), recall.getCandidateJobId(), e.getMessage());
                log.error(errorMsg, e);
                XxlJobHelper.log(errorMsg);
                errorMessages.add(errorMsg);
                // 继续处理下一个记录
            }
        }
        
        String summary = String.format("Reject unresponsive candidates completed. Success: %d, Failure: %d", 
                successCount, failureCount);
        log.info(summary);
        XxlJobHelper.log(summary);
        
        return errorMessages;
    }

    /**
     * 判断职位所在地是否在中国
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
            
            return isChina;
        } catch (Exception e) {
            log.error("Error checking job location for countryId: {}", jobCountryId, e);
            return false;
        }
    }

    /**
     * 构建短信内容
     */
    private String buildSmsMessage(boolean isChinese, String candidateName, String companyName, 
                                   String jobTitle, String phoneNumber) throws IOException {
        try {
            String templatePath = isChinese 
                    ? "templates/sms-interview-followup-zh.txt" 
                    : "templates/sms-interview-followup-en.txt";
            
            ClassPathResource resource = new ClassPathResource(templatePath);
            String template;
            try (InputStream inputStream = resource.getInputStream()) {
                template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            
            String genderTitle = isChinese ? "先生/女士" : "Mr./Ms.";
            template = template.replace("{candidateName}", candidateName);
            template = template.replace("{genderTitle}", genderTitle);
            template = template.replace("{companyName}", companyName);
            template = template.replace("{jobTitle}", jobTitle);
            template = template.replace("{phoneNumber}", phoneNumber);
            
            return template;
        } catch (Exception e) {
            log.error("Failed to load SMS template. isChinese: {}", isChinese, e);
            throw e;
        }
    }

}
