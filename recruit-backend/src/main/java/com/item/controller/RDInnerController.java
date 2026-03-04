package com.item.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.entity.AiCallbackEntity;
import com.item.entity.AiVettedResultEntity;
import com.item.framework.annotation.Auth;
import com.item.framework.config.RecruitCommonNacosConfig;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.constant.RoleType;
import com.item.framework.error.BusinessException;
import com.item.mapper.AICallbackMapper;
import com.item.mapper.AiVettedResultMapper;
import com.item.service.AIService;
import com.item.service.JobRecommendService;
import com.item.service.PointService;
import com.item.service.AiScreeningSyncService;
import com.item.service.ApplicationsSyncService;
import com.item.service.InterviewResultSyncService;
import com.item.service.RDDomainService;
import com.item.service.UploadService;
import com.item.util.CommonUtils;
import com.item.vo.ApplicationsSyncVO;
import com.item.vo.RecommendCandidateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author : lh
 * 清洗数据接口 仅开发使用 不对前端暴露
 */
@Slf4j
@RestController
@RequestMapping("/api/rd/inner")
@RequiredArgsConstructor
public class RDInnerController {

    private final RDDomainService rdDomainService;
    private final RecruitCommonNacosConfig commonNacosConfig;
    private final UploadService uploadService;
    private final AICallbackMapper aicallbackMapper;
    private final AiVettedResultMapper aiVettedResultMapper;
    private final AIService aiService;
    private final JobRecommendService jobRecommendService;
    private final PointService pointService;
//    private final ApplicationsSyncService applicationsSyncService;
//    private final InterviewResultSyncService interviewResultSyncService;
//    private final AiScreeningSyncService aiScreeningSyncService;

    @GetMapping("/update-job-location")
    public void updateJobLocation(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") Integer pageIndex,
                                  @RequestParam(value = "pageSize", required = false, defaultValue = "300") Integer pageSize,
                                  @RequestParam(value = "currentJobId", required = false) Long currentJobId,
                                  @RequestParam(value = "security") String security) {

        if (!isAccess(security)) {
            return;
        }
        rdDomainService.updateJobEsOld2NewLocation(pageIndex, pageSize, currentJobId);
    }

    @GetMapping("/update-interview-length")
    public void updateJobInterviewLength(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") Integer pageIndex,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "300") Integer pageSize,
                                         @RequestParam(value = "currentJobId", required = false) Long currentJobId,
                                         @RequestParam(value = "defaultInterviewLength", required = false) Integer defaultInterviewLength,
                                         @RequestParam(value = "security") String security) {

        if (!isAccess(security)) {
            return;
        }
        rdDomainService.updateJobInterviewLength(pageIndex, pageSize, currentJobId, defaultInterviewLength);
    }

    @PostMapping("/ayrshare/image")
    public String uploadCompanyLogo(@RequestPart("file") MultipartFile file, @RequestParam(value = "security") String security) {
        if (!isAccess(security)) {
            return "";
        }
        CommonUtils.validateFileFormatAndSize(file);
        try {
            return /*uploadService.uploadImage(file)*/null;
        } catch (Exception e) {
            throw BusinessException.of(CommonResponseCode.COMMON_COMPANY_LOGO_UPLOAD_FILE_FAIL);
        }
    }

    private boolean isAccess(String security) {
        String rdControllerSecurity = commonNacosConfig.getRdControllerSecurity();
        if (StringUtils.isBlank(security) || StringUtils.isNotBlank(security) && !Strings.CS.equals(rdControllerSecurity, security)) {
            log.warn("RDInnerController updateJobLocation rdControllerSecurity {} security {}", rdControllerSecurity, security);
            return false;
        }
        return true;
    }

    /**
     * 获取所有需要生成面试报告的数据
     * 从aiVettedResultMapper获取interviewReportUrl为空的数据，
     * 然后根据candidateJobId获取aicallbackMapper中对应的数据
     */
    @GetMapping("/getall-interview-report")
    public List<AiCallbackEntity> getAllInterviewReport(@RequestParam(value = "security") String security){
        if (!isAccess(security)) {
            return new ArrayList<>();
        }
        try {
            LambdaQueryWrapper<AiVettedResultEntity> vettedWrapper = new LambdaQueryWrapper<>();
            vettedWrapper.select(AiVettedResultEntity::getId, AiVettedResultEntity::getCandidateJobId)
                         .and(wrapper -> wrapper.isNull(AiVettedResultEntity::getInterviewReportUrl)
                                                .or()
                                                .eq(AiVettedResultEntity::getInterviewReportUrl, ""))
                         .orderByDesc(AiVettedResultEntity::getId);
            
            List<AiVettedResultEntity> vettedResults = aiVettedResultMapper.selectList(vettedWrapper);
            log.info("Found {} AI vetted results with empty interviewReportUrl", vettedResults.size());
            
            if (vettedResults.isEmpty()) {
                log.info("No AI vetted results found with empty interviewReportUrl");
                return new ArrayList<>();
            }
            
            List<AiCallbackEntity> resultList = processCallbacksAndGenerateReports(vettedResults);
            
            log.info("Retrieved {} AI callback entities from {} vetted results", resultList.size(), vettedResults.size());
            return resultList;
            
        } catch (Exception e) {
            log.error("Error retrieving interview report data", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 处理回调数据并生成报告
     * @param vettedResults AI审核结果列表
     * @return 处理后的回调实体列表
     */
    private List<AiCallbackEntity> processCallbacksAndGenerateReports(List<AiVettedResultEntity> vettedResults) {
        List<AiCallbackEntity> resultList = new ArrayList<>();
        
        // 提取所有candidateJobId
        List<Long> candidateJobIds = vettedResults.stream()
                .map(AiVettedResultEntity::getCandidateJobId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        
        if (candidateJobIds.isEmpty()) {
            return resultList;
        }
        
        // 批量查询所有相关的callback记录
        Map<Long, AiCallbackEntity> latestCallbackMap = getLatestCallbackMap(candidateJobIds);
        
        // 统计处理结果
        int successCount = 0;
        int errorCount = 0;
        
        // 按原始vettedResults的顺序返回结果
        for (AiVettedResultEntity vettedResult : vettedResults) {
            if (vettedResult.getCandidateJobId() != null) {
                AiCallbackEntity callback = latestCallbackMap.get(vettedResult.getCandidateJobId());
                if (callback != null) {
                    resultList.add(callback);

                    // 生成并更新面试报告
                    if (generateAndUpdateInterviewReport(vettedResult, callback)) {
                        successCount++;
                    } else {
                        errorCount++;
                    }
                    
                    log.debug("Found callback for candidateJobId: {}, callId: {}", 
                            vettedResult.getCandidateJobId(), callback.getCallId());
                } else {
                    log.warn("No callback found for candidateJobId: {}", vettedResult.getCandidateJobId());
                }
            }
        }
        
        log.info("Batch query optimization: Retrieved {} callbacks from {} candidate job IDs", 
                resultList.size(), candidateJobIds.size());
        log.info("Interview report generation completed. Success: {}, Errors: {}, Total processed: {}", 
                successCount, errorCount, resultList.size());
        
        return resultList;
    }
    
    /**
     * 获取最新的回调数据映射
     * @param candidateJobIds 候选人工作ID列表
     * @return 候选人工作ID到最新回调实体的映射
     */
    private java.util.Map<Long, AiCallbackEntity> getLatestCallbackMap(List<Long> candidateJobIds) {
        LambdaQueryWrapper<AiCallbackEntity> callbackWrapper = new LambdaQueryWrapper<>();
        callbackWrapper.in(AiCallbackEntity::getCandidateJobId, candidateJobIds)
                      .orderByAsc(AiCallbackEntity::getCandidateJobId)
                      .orderByDesc(AiCallbackEntity::getCreateTime);
        
        List<AiCallbackEntity> allCallbacks = aicallbackMapper.selectList(callbackWrapper);
        
        // 按candidateJobId分组，取每组最新的记录
        return allCallbacks.stream()
                .collect(java.util.stream.Collectors.toMap(
                        AiCallbackEntity::getCandidateJobId,
                        callback -> callback,
                        (existing, replacement) -> existing, // 保留第一个（最新的）
                        java.util.LinkedHashMap::new
                ));
    }
    
    /**
     * 生成并更新面试报告
     * @param vettedResult AI审核结果
     * @param callback 回调实体
     * @return 是否成功
     */
    private boolean generateAndUpdateInterviewReport(AiVettedResultEntity vettedResult, AiCallbackEntity callback) {
        try {
            // 生成面试报告
            String interviewReportUrl = aiService.exportInterviewReport(callback.getCallId());
            
            // 更新AiVettedResult表中的interviewReportUrl
            if (interviewReportUrl != null && !interviewReportUrl.trim().isEmpty()) {
                vettedResult.setInterviewReportUrl(interviewReportUrl);
                vettedResult.setUpdateTime(LocalDateTime.now());
                aiVettedResultMapper.updateById(vettedResult);
                log.info("Generated and updated interview report for candidateJobId: {}, URL: {}",
                        vettedResult.getCandidateJobId(), interviewReportUrl);
                return true;
            } else {
                log.warn("Generated empty interview report URL for candidateJobId: {}", 
                        vettedResult.getCandidateJobId());
                return false;
            }
        } catch (Exception e) {
            log.error("Error generating interview report for candidateJobId: {}, callId: {}", 
                    vettedResult.getCandidateJobId(), callback.getCallId(), e);
            return false;
        }
    }


    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/recommend-candidates")
    public List<RecommendCandidateVO> recommendCandidates(@RequestParam(value = "security") String security,
                                                          @RequestParam(value = "jobId") Long jobId) {
        if (!isAccess(security)) {
            return new ArrayList<>();
        }
        return jobRecommendService.recommendCandidates(jobId);
    }


    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/update-job-check-points")
    public boolean updateJobCheckPoints(@RequestParam(value = "security") String security,
                                        @RequestParam(value = "jobId") Long jobId,
                                        @RequestParam(value = "companyCode") String companyCode,
                                        @RequestParam(value = "oldInterviewType") Integer oldInterviewType,
                                        @RequestParam(value = "newInterviewType") Integer newInterviewType) {
        if (!isAccess(security)) {
            throw new BusinessException(CommonResponseCode.COMMON_USER_IDENTIFY_NOT_INVALID);
        }
        return pointService.updateJobCheckPoints(jobId,companyCode,oldInterviewType,newInterviewType);
    }


    @GetMapping("/users/primary")
    public Long getUserPrimary(@RequestParam(value = "security") String security,
                                  @RequestParam(value = "comanyCode") String comanyCode) {

        if (!isAccess(security)) {
            throw new BusinessException(CommonResponseCode.COMMON_USER_IDENTIFY_NOT_INVALID);
        }
        return pointService.getUserPrimaryId(comanyCode);
    }


//    /**
//     * 同步菲律宾关联表数据
//     * @param syncVo
//     * @return
//     */
//    @PostMapping("/application-sync")
//    public boolean applicationSync (@RequestBody ApplicationsSyncVO syncVo){
//        if (!isAccess(syncVo.getSecurity())) {
//            return false;
//        }
//        return applicationsSyncService.applicationSync(syncVo);
//    }
//
//    /**
//     * 检查未同步关联表id
//     * @param security
//     * @return
//     */
//    @GetMapping("/not-application-sync")
//    public List<String> notApplicationSync (@RequestParam(value = "security") String security){
//        if (!isAccess(security)) {
//            throw new BusinessException(CommonResponseCode.COMMON_USER_IDENTIFY_NOT_INVALID);
//        }
//        return applicationsSyncService.getNotApplicationSync();
//    }
//
//
//    /**
//     * 同步菲律宾面试结果数据
//     * @param syncVo
//     * @return
//     */
//    @PostMapping("/interview-result-sync")
//    public boolean interviewResultSync (@RequestBody ApplicationsSyncVO syncVo){
//        if (!isAccess(syncVo.getSecurity())) {
//            return false;
//        }
//        return interviewResultSyncService.interviewResultSync(syncVo);
//    }
//
//    /**
//     * 检查未同步
//     * @param security
//     * @return
//     */
//    @GetMapping("/not-interview-result-sync")
//    public List<String> notInterviewResultSync (@RequestParam(value = "security") String security){
//        if (!isAccess(security)) {
//            throw new BusinessException(CommonResponseCode.COMMON_USER_IDENTIFY_NOT_INVALID);
//        }
//        return interviewResultSyncService.notInterviewResultSync();
//    }
//
//    /**
//     * 同步菲律宾ai筛选结果数据
//     * @param syncVo
//     * @return
//     */
//    @PostMapping("/ai-screening-sync")
//    public boolean aiScreeningSync (@RequestBody ApplicationsSyncVO syncVo){
//        if (!isAccess(syncVo.getSecurity())) {
//            return false;
//        }
//        return aiScreeningSyncService.aiScreeningSync(syncVo);
//    }
//
//    /**
//     * 检查未同步
//     * @param security
//     * @return
//     */
//    @GetMapping("/not-ai-screening-sync")
//    public List<String> notAiScreeningSync (@RequestParam(value = "security") String security){
//        if (!isAccess(security)) {
//            throw new BusinessException(CommonResponseCode.COMMON_USER_IDENTIFY_NOT_INVALID);
//        }
//        return aiScreeningSyncService.notAiScreeningSync();
//    }


}


