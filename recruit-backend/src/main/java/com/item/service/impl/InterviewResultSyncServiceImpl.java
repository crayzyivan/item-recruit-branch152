package com.item.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.entity.AiVettedResultEntity;
import com.item.entity.AiVettedResultSkillEntity;
import com.item.entity.ApplicationInterviewReportsEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.framework.constant.CommonConstants;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.constant.DataSourceEnum;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.framework.constant.SkillLevelEnum;
import com.item.framework.constant.SoftSkillLevelEnum;
import com.item.framework.error.BusinessException;
import com.item.service.AiVettedResultService;
import com.item.service.AiVettedResultSkillService;
import com.item.service.ApplicationInterviewReportsService;
import com.item.service.CandidateJobService;
import com.item.service.InterviewResultSyncService;
import com.item.service.migration.DataMigrationMappingService;
import com.item.util.JsonUtils;
import com.item.util.PdfModificationUtils;
import com.item.util.S3Utils;
import com.item.util.VideoUtils;
import com.item.vo.ApplicationsSyncVO;
import com.item.vo.sync.SkillEvaluationDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 面试结果同步服务实现类
 * 
 * 负责将PostgreSQL中的面试结果数据同步到MySQL数据库。
 * 支持从candidates.application_interview_reports表同步数据到r_ai_vetted_result表。
 * 实现幂等性设计，支持重复调用而不产生副作用。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-29
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class InterviewResultSyncServiceImpl implements InterviewResultSyncService {

    private final ApplicationInterviewReportsService applicationInterviewReportsService;
    private final AiVettedResultService aiVettedResultService;
    private final AiVettedResultSkillService aiVettedResultSkillService;
    private final RestTemplate restTemplate;
    private final S3Utils s3Utils;
    private final VideoUtils videoUtils;
    private final CandidateJobService candidateJobService;
    private final DataMigrationMappingService dataMigrationMappingService;

    @Value("${recruit.video-prefix-url:}")
    private String videoPrefixUrl;
    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncInterviewResultFromPg(Long id, String applicationId) {
        log.info("Starting interview result sync: id={}, applicationId={}", id, applicationId);
        try {
            // 1. 参数验证
            if (StringUtils.isEmpty(applicationId)){
                return false;
            }
            Optional<DataMigrationMappingEntity> mappingEntityOptional = dataMigrationMappingService.findByPgsqlIdAndType(applicationId, MigrationBusTypeEnum.CANDIDATE_JOB);
            if(mappingEntityOptional.isEmpty()){
                return false;
            }
            Long candidateJobId=mappingEntityOptional.get().getMysqlId();
            // 2. 删除旧的面试结果
            AiVettedResultEntity existingResult = aiVettedResultService.selectByCandidateJobId(candidateJobId);
            if (existingResult != null) {
                aiVettedResultSkillService.deleteByVettedResultId(existingResult.getId());
                aiVettedResultService.removeById(existingResult.getId());
            }

            // 3. 从PostgreSQL查询面试结果数据,取最新的面试报告（按创建时间倒序排列，取第一个）
            ApplicationInterviewReportsEntity report = applicationInterviewReportsService.getByApplicationId(UUID.fromString(applicationId));
            
            if (report==null) {
                log.info("No interview reports found in PostgreSQL for applicationId={}", applicationId);
                throw BusinessException.of(CommonResponseCode.INTERVIEW_RESULT_NOT_FOUND);
            }
            // 5. 手动数据转换面试结果
            AiVettedResultEntity aiVettedResult = convertToAiVettedResult(report, candidateJobId);
            log.info("Successfully converted interview report to AI vetted result: candidateJobId={}",candidateJobId);
            aiVettedResultService.save(aiVettedResult);
            // 6. 手动数据转换面试结果技能
            List<AiVettedResultSkillEntity> skillsEntityList=convertToAiVettedResultSkills(aiVettedResult.getId(),report);
            aiVettedResultSkillService.saveBatch(skillsEntityList);
            log.info("Successfully synced interview result: candidateJobId={}, applicationId={}",
                    candidateJobId, applicationId);
            // 7. 更新关联表信息
            CandidateJobEntity candidateJob = candidateJobService.getById(candidateJobId);
            if (candidateJob!=null){
                candidateJob.setInterviewMailStatus(1);
                candidateJob.setInterviewTime(aiVettedResult.getInterviewTime());
                candidateJob.setOverallScore(aiVettedResult.getInterviewScore());
                candidateJob.setCheatingAnalysis(aiVettedResult.getProctoringScore());
                candidateJob.setCameraRecordingUrl(aiVettedResult.getCameraRecordingUrl());
                candidateJobService.updateById(candidateJob);
            }
            // 8.迁移记录
            dataMigrationMappingService.saveMapping(applicationId,candidateJobId, MigrationBusTypeEnum.INTERVIEW_REPORTS);
            return true;
        } catch (Exception e) {
            log.error("Unexpected error during interview result sync: id={}, applicationId={}, errorType={}, errorMessage={}",
                    id, applicationId, e.getClass().getSimpleName(), e.getMessage(), e);
            throw BusinessException.of(CommonResponseCode.INTERVIEW_RESULT_SYNC_FAILED);
        }
    }


    /**
     * 手动转换PostgreSQL面试报告实体到MySQL AI审核结果实体
     * 
     * @param source PostgreSQL面试报告实体
     * @param candidateJobId 候选人职位关联ID
     * @return MySQL AI审核结果实体
     */
    private AiVettedResultEntity convertToAiVettedResult(ApplicationInterviewReportsEntity source, Long candidateJobId) {
        log.debug("Converting interview report to AI vetted result: reportId={}, candidateJobId={}", 
                source.getId(), candidateJobId);
        AiVettedResultEntity target = new AiVettedResultEntity();

        // 设置候选人职位关联ID
        target.setCandidateJobId(candidateJobId);
        // 时间类型转换：OffsetDateTime -> LocalDateTime
        if (source.getCreatedOn() != null) {
            target.setInterviewTime(source.getReportDate().toLocalDateTime());
        }
        // 直接映射的字段
        target.setInterviewScore(source.getInterviewScore());
        target.setProctoringScore(source.getProctoringScore());
        //转换对话
        target.setTranscript(convertInterviewTranscript(source.getInterviewTranscript()));
        //转换视频地址
        String videoFileName = videoUtils.generateVideoFileName(source.getApplicationId());
        String s3Key = s3Utils.getS3Key(videoFileName);
        if (s3Utils.isS3KeyExists(s3Key)){
            target.setCameraRecordingUrl(s3Key);
        }else{
            target.setCameraRecordingUrl(downloadAndUploadVideoToS3(source.getInterviewRecordingUrl(),candidateJobId,source.getApplicationId()));
        }
        //转换报告地址
        String pdfFileUrl = getPdfFileUrl(source.getApplicationId());
        String pdfS3Key = s3Utils.getS3Key(pdfFileUrl);
        if (s3Utils.isS3KeyExists(pdfS3Key)){
            target.setInterviewReportUrl(pdfS3Key);
        }else{
            target.setInterviewReportUrl(downloadAndUploadReportToS3(source.getReportUrl(),candidateJobId,source.getApplicationId()));
        }
        //笔试结果
        SkillEvaluationDTO.AiEvaluationDTO aiEvaluation=convertJsonToAiEvaluation(source.getCodingSkillsEvaluation());
        if (aiEvaluation!=null){
            target.setOverallSkillAssessment(aiEvaluation.getFeedback());
            target.setOverallSkillLevel(convertSkillLevel(aiEvaluation.getRating()));
        }
        target.setCreateTime(LocalDateTime.now());
        target.setUpdateTime(LocalDateTime.now());
        target.setDataSource(DataSourceEnum.PHL.getSource());
        log.debug("Successfully converted interview report: reportId={} -> candidateJobId={}",
                source.getId(), candidateJobId);
        return target;
    }

    /**
     * 将JSON字符串转换为AiEvaluationDTO对象
     *
     * @param jsonString JSON格式的评估数据
     * @return AiEvaluationDTO对象
     * @throws IllegalArgumentException 如果JSON格式无效
     */
    private SkillEvaluationDTO.AiEvaluationDTO convertJsonToAiEvaluation(String jsonString) {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        try {
            // 使用JsonUtils直接转换
            SkillEvaluationDTO.AiEvaluationDTO aiEvaluation =
                    JsonUtils.toObject(jsonString, SkillEvaluationDTO.AiEvaluationDTO.class);
            log.debug("Successfully converted JSON to AiEvaluationDTO: rating={}, feedback length={}",
                    aiEvaluation.getRating(),
                    aiEvaluation.getFeedback() != null ? aiEvaluation.getFeedback().length() : 0);
            return aiEvaluation;
        } catch (Exception e) {
            log.error("Failed to convert JSON to AiEvaluationDTO: {}", jsonString, e);
            return null;
        }
    }


    /**
     * 转换PostgreSQL面试报告实体到MySQL AI审核结果明细实体
     *
     * @param report PostgreSQL面试报告实体
     * @param vettedResultId AI审核结果表id
     * @return MySQL AI审核结果明细实体
     */
    private List<AiVettedResultSkillEntity> convertToAiVettedResultSkills(Long vettedResultId, ApplicationInterviewReportsEntity report) {
        List<AiVettedResultSkillEntity> resultSkills=new ArrayList<>();
        if (StringUtils.isNotEmpty(report.getTechnicalSkillsEvaluation())){
            // 遍历技能评估
            List<SkillEvaluationDTO> evaluations = JsonUtils.toObject(
                    report.getTechnicalSkillsEvaluation(),
                    new TypeReference<List<SkillEvaluationDTO>>() {}
            );
            if (!CollectionUtils.isEmpty(evaluations)){
                for (SkillEvaluationDTO evaluation : evaluations){
                    AiVettedResultSkillEntity skill=new AiVettedResultSkillEntity();
                    skill.setVettedResultId(vettedResultId);
                    skill.setSkillName(evaluation.getSkill());
                    skill.setSkillAssessment(evaluation.getAiEvaluation().getFeedback());
                    skill.setSkillLevel(convertSkillLevel(evaluation.getAiEvaluation().getRating()));
                    skill.setCreateTime(LocalDateTime.now());
                    skill.setUpdateTime(LocalDateTime.now());
                    resultSkills.add(skill);
                }
            }
            //软技能
            List<SkillEvaluationDTO> softEvaluations = JsonUtils.toObject(
                    report.getSoftSkillsEvaluation(),
                    new TypeReference<List<SkillEvaluationDTO>>() {}
            );
            if (!CollectionUtils.isEmpty(softEvaluations)){
                SkillEvaluationDTO evaluation = softEvaluations.getFirst();
                AiVettedResultSkillEntity skill=new AiVettedResultSkillEntity();
                skill.setVettedResultId(vettedResultId);
                skill.setSkillName(CommonConstants.AI_VETTED_SOFT_SKILL_NAME);
                skill.setSkillAssessment(evaluation.getAiEvaluation().getFeedback());
                skill.setSkillLevel(convertSoftSkillLevel(evaluation.getAiEvaluation().getRating()));
                skill.setCreateTime(LocalDateTime.now());
                skill.setUpdateTime(LocalDateTime.now());
                resultSkills.add(skill);
            }
        }
        return resultSkills;
    }

    /**
     * 将SkillEvaluationDTO中的技能等级字符串转换为数字
     *
     * @param rating 技能等级字符串
     * @return 技能等级数字
     */
    private Integer convertSkillLevel(String rating) {
        if (rating.equals("Not experienced")) {
            return SkillLevelEnum.NOT_EXPERIENCED.getLevel();
        } else if (rating.equals("Junior")) {
            return SkillLevelEnum.JUNIOR.getLevel();
        } else if (rating.equals("Mid-level")) {
            return SkillLevelEnum.MID_LEVEL.getLevel();
        } else if (rating.equals("Senior")) {
            return SkillLevelEnum.SENIOR.getLevel();
        }else {
            return null;
        }
    }

    /**
     * 将SkillEvaluationDTO中的软技能等级字符串转换为数字
     *
     * @param rating 技能等级字符串
     * @return 技能等级数字
     */
    private Integer convertSoftSkillLevel(String rating) {
        if ("Not Experienced".equals(rating)) {
            return SoftSkillLevelEnum.A1.getLevel();
        }else if (SoftSkillLevelEnum.C1.getName().equals( rating)) {
            return SoftSkillLevelEnum.A2.getLevel();
        }else if (SoftSkillLevelEnum.B1.getName().equals( rating)) {
            return SoftSkillLevelEnum.B1.getLevel();
        }else if (SoftSkillLevelEnum.B2.getName().equals( rating)) {
            return SoftSkillLevelEnum.B2.getLevel();
        }else if ("Good".equals(rating)) {
            return SoftSkillLevelEnum.C1.getLevel();
        }else if ("Excellent".equals(rating)) {
            return SoftSkillLevelEnum.C2.getLevel();
        }else {
            return null;
        }
    }

    /**
     * 转换面试对话JSON中的角色字段并移除timestamp字段
     * 将 "interviewer" 转换为 "Agent"，"user" 转换为 "User"，移除 "timestamp" 字段
     *
     * @param interviewJson 原始面试对话JSON字符串
     * @return 转换后的JSON字符串
     * @throws IllegalArgumentException 如果JSON格式无效
     */
    private String convertInterviewTranscript(String interviewJson) {
        try {
            // 解析JSON数组
            ObjectMapper objectMapper = JsonUtils.getObjectMapper();
            JsonNode rootNode = objectMapper.readTree(interviewJson);
            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("The input JSON must be in array format.");
            }
            // 创建新的JSON数组来存储转换后的数据
            com.fasterxml.jackson.databind.node.ArrayNode resultArray = objectMapper.createArrayNode();

            // 遍历每个对话对象
            for (JsonNode dialogNode : rootNode) {
                com.fasterxml.jackson.databind.node.ObjectNode newDialog = objectMapper.createObjectNode();
                // 转换role字段为spokesperson字段
                JsonNode roleNode = dialogNode.get("role");
                if (roleNode != null) {
                    String originalRole = roleNode.asText();
                    String convertedRole = convertRole(originalRole);
                    newDialog.put("spokesperson", convertedRole);
                }

                // 复制content字段
                JsonNode contentNode = dialogNode.get("content");
                if (contentNode != null) {
                    newDialog.put("content", contentNode.asText());
                }
                // 注意：故意不复制timestamp字段，实现移除效果
                resultArray.add(newDialog);
            }
            return objectMapper.writeValueAsString(resultArray);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON conversion failed: " + e.getMessage(), e);
        }
    }

    /**
     * 转换单个角色名称
     *
     * @param originalRole 原始角色名称
     * @return 转换后的角色名称
     */
    private static String convertRole(String originalRole) {
        if (originalRole == null) {
            return "";
        }
        switch (originalRole.toLowerCase()) {
            case "interviewer":
                return "Agent";
            case "user":
                return "User";
            default:
                return originalRole;
        }
    }

    /**
     * 下载面试报告并上传到S3
     *
     * @param reportUrl 原始报告URL
     * @param candidateJobId 候选人职位关联ID
     * @return S3中的文件key，如果失败则返回原始URL
     */
    private String downloadAndUploadReportToS3(String reportUrl, Long candidateJobId,String applicationId) {
        // 如果原始URL为空，直接返回空字符串
        if (StringUtils.isBlank(reportUrl)) {
            return "";
        }
        try {
            log.info("Starting to download and upload report to S3: candidateJobId={}, originalUrl={}",
                    candidateJobId, reportUrl);
            // 使用RestTemplate下载文件
            ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(reportUrl, byte[].class);

            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                log.warn("Failed to download report for candidateJobId={}, status={}, returning original URL",
                        candidateJobId, responseEntity.getStatusCode());
                return reportUrl;
            }
            byte[] fileContent = responseEntity.getBody();
            //修改PDF中的链接
            HashMap<String, String> linkReplacements = new HashMap<>();
            String newUrl=videoPrefixUrl+"/"+applicationId;
            linkReplacements.put("http", newUrl);
            byte[] modifiedPdfBytes = PdfModificationUtils.modifyPdfLinks(fileContent, linkReplacements);
            // 生成S3文件名
            String fileName = activeProfile+"/PHL_interview_report_" + applicationId + ".pdf";
            // 上传到S3
            String s3Key = s3Utils.uploadFromByteArray(modifiedPdfBytes, fileName, "application/pdf");
            log.info("Successfully uploaded report to S3 for candidateJobId={}, s3Key={}", candidateJobId, s3Key);
            return s3Key;
        } catch (Exception e) {
            log.error("Failed to download and upload report to S3 for candidateJobId={}, originalUrl={}, error={}",
                    candidateJobId, reportUrl, e.getMessage(), e);
            return "";
        }
    }

    /**
     * 获取菲律宾面试pdf地址
     * @param applicationId
     * @return
     */
    private String getPdfFileUrl (String applicationId){
        return activeProfile+"/PHL_interview_report_" + applicationId + ".pdf";
    }


    /**
     * 下载m3u8视频并上传到S3
     *
     * @param videoUrl 原始视频URL（m3u8格式）
     * @param candidateJobId 候选人职位关联ID
     * @return S3中的文件key，如果失败则返回原始URL
     */
    public String downloadAndUploadVideoToS3(String videoUrl, Long candidateJobId,String applicationId) {
        // 如果原始URL为空，直接返回空字符串
        if (StringUtils.isBlank(videoUrl)) {
            return "";
        }
        // 检查是否为m3u8格式
        if (!videoUrl.toLowerCase().contains(".m3u8")) {
            return videoUrl;
        }
        try {
            log.info("Starting to download and upload M3U8 video to S3: candidateJobId={}, originalUrl={}",
                    candidateJobId, videoUrl);
            // 1. 下载并合并m3u8视频
            byte[] videoContent = videoUtils.downloadAndMergeFFmpegM3u8ToMp4(videoUrl, candidateJobId);
            log.info("Successfully downloaded and merged M3U8 video for candidateJobId={}, size={} bytes", candidateJobId, videoContent.length);
            // 2. 生成S3文件名
            String fileName = videoUtils.generateVideoFileName(applicationId);
            // 3. 上传到S3
            String s3Key = s3Utils.uploadFromByteArray(videoContent, fileName, "video/mp4");
            log.info("Successfully uploaded M3U8 video to S3 for candidateJobId={}, s3Key={}", candidateJobId, s3Key);
            return s3Key;
        } catch (Exception e) {
            log.error("Failed to download and upload M3U8 video to S3 for candidateJobId={}, originalUrl={}, error={}",
                    candidateJobId, videoUrl, e.getMessage(), e);
            // 发生错误时返回原始URL，确保数据不丢失
            log.warn("Returning original URL due to error: {}", videoUrl);
            return videoUrl;
        }
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

    /**
     * 同步面试结果数据
     * @param syncVo
     * @return
     */
    @Override
    public boolean interviewResultSync(ApplicationsSyncVO syncVo) {
        List<ApplicationInterviewReportsEntity> list = applicationInterviewReportsService.listBySyncVo(syncVo);
        if (!CollectionUtils.isEmpty(list)) {
            for (ApplicationInterviewReportsEntity reportsEntity:list){
                syncInterviewResultFromPg(reportsEntity.getId(),reportsEntity.getApplicationId());
            }
        }
        return true;
    }

    /**
     * 未同步的id
     * @return
     */
    @Override
    public List<String> notInterviewResultSync() {
        List<String> applicationIds=new ArrayList<>();
        List<ApplicationInterviewReportsEntity> list = applicationInterviewReportsService.listAll();
        List<DataMigrationMappingEntity> candidateJobList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.CANDIDATE_JOB);
        List<DataMigrationMappingEntity> interviewList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.INTERVIEW_REPORTS);

        List<String> pgApplicationIds=interviewList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();
        List<String> pgCandidateJobIds=candidateJobList.stream().map(DataMigrationMappingEntity::getPgsqlId).toList();

        for (ApplicationInterviewReportsEntity reportsEntity:list){
            if (!pgApplicationIds.contains(reportsEntity.getApplicationId())){
                if (pgCandidateJobIds.contains(reportsEntity.getApplicationId())){
                    applicationIds.add(reportsEntity.getApplicationId());
                }
            }
        }
        return applicationIds;
    }
}
