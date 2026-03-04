package com.item.schedule;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.item.entity.AiVettedResultEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.framework.constant.MigrationBusTypeEnum;
import com.item.service.AiVettedResultService;
import com.item.service.migration.DataMigrationMappingService;
import com.item.task.core.handler.annotation.ScheduleTask;
import com.item.util.PdfModificationUtils;
import com.item.util.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 更新菲律宾同步数据pdf的视频地址
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-11-20  10:38
 */
@Component
@Slf4j
@RefreshScope
@RequiredArgsConstructor
public class UpdatePdfUrlJob {
    private final DataMigrationMappingService dataMigrationMappingService;
    private final AiVettedResultService aiVettedResultService;
    private final S3Utils s3Utils;
    @Value("${recruit.video-prefix-url:}")
    private String videoPrefixUrl;

    @ScheduleTask("updatePdfUrlHandler")
    public void updatePdfUrlHandler() {
        log.info("updatePdfUrlHandler start:");
        List<DataMigrationMappingEntity> mappingEntityList = dataMigrationMappingService.findAllByType(MigrationBusTypeEnum.INTERVIEW_REPORTS);
        if(CollectionUtils.isNotEmpty(mappingEntityList)) {
            log.info("updatePdfUrlHandler size:{}", mappingEntityList.size());
            
            // 构建 mysqlId -> DataMigrationMappingEntity 的映射
            Map<Long, DataMigrationMappingEntity> migrationMappingEntityMap = mappingEntityList.stream()
                    .collect(Collectors.toMap(
                            DataMigrationMappingEntity::getMysqlId,
                            entity -> entity,
                            (existing, replacement) -> existing
                    ));
            
            List<Long> candidateIds = mappingEntityList.stream().map(DataMigrationMappingEntity::getMysqlId).toList();
            List<AiVettedResultEntity> aiVettedResultEntities = aiVettedResultService.listByCandidateJobIds(candidateIds);
            if (CollectionUtils.isNotEmpty(aiVettedResultEntities)){
                for (AiVettedResultEntity result : aiVettedResultEntities) {
                    try {
                        updatePdfVideoUrl(result, migrationMappingEntityMap);
                    } catch (Exception e) {
                        log.error("updatePdfUrlHandler failed for candidateJobId:{}, error:{}", 
                                result.getCandidateJobId(), e.getMessage(), e);
                    }
                }
            }
        }
        log.info("updatePdfUrlHandler end:");
    }

    /**
     * 更新PDF中的视频地址
     * @param result AI审核结果实体
     * @param migrationMappingEntityMap 迁移映射关系Map
     */
    private void updatePdfVideoUrl(AiVettedResultEntity result, Map<Long, DataMigrationMappingEntity> migrationMappingEntityMap) {
        if (result == null) {
            log.warn("updatePdfVideoUrl: result is null");
            return;
        }

        String interviewReportUrl = result.getInterviewReportUrl();
        Long candidateJobId = result.getCandidateJobId();
        
        // 验证必要字段
        if (!StringUtils.hasText(interviewReportUrl)) {
            log.warn("updatePdfVideoUrl: interviewReportUrl is empty for candidateJobId:{}", candidateJobId);
            return;
        }

        // 从映射关系中获取 pgsqlId (applicationId)
        DataMigrationMappingEntity mappingEntity = migrationMappingEntityMap.get(candidateJobId);
        if (mappingEntity == null || !StringUtils.hasText(mappingEntity.getPgsqlId())) {
            log.warn("updatePdfVideoUrl: cannot find pgsqlId for candidateJobId:{}", candidateJobId);
            return;
        }
        
        String applicationId = mappingEntity.getPgsqlId();
        log.info("updatePdfVideoUrl start: candidateJobId={}, applicationId={}, pdfUrl={}", 
                candidateJobId, applicationId, interviewReportUrl);

        // 1. 从S3下载PDF文件
        byte[] pdfBytes = s3Utils.downloadFileAsBytes(interviewReportUrl);
        if (pdfBytes == null || pdfBytes.length == 0) {
            log.error("updatePdfVideoUrl: failed to download PDF from S3, key={}", interviewReportUrl);
            return;
        }

        // 2. 构建链接替换映射
        Map<String, String> linkReplacements = new HashMap<>();
        String newUrl = videoPrefixUrl + "/" + applicationId;
        linkReplacements.put("http", newUrl);
        log.info("updatePdfVideoUrl: replace video url to: {}", newUrl);
        
        // 3. 修改PDF中的链接
        byte[] modifiedPdfBytes = PdfModificationUtils.modifyPdfLinks(pdfBytes, linkReplacements);

        // 4. 将修改后的PDF上传回S3（原地更新）
        boolean updateSuccess = s3Utils.updateFileInPlace(
                interviewReportUrl, 
                modifiedPdfBytes, 
                "application/pdf"
        );
        
        if (updateSuccess) {
            log.info("updatePdfVideoUrl success: candidateJobId={}, applicationId={}, pdfUrl={}", 
                    candidateJobId, applicationId, interviewReportUrl);
        } else {
            log.error("updatePdfVideoUrl failed: candidateJobId={}, applicationId={}, pdfUrl={}", 
                    candidateJobId, applicationId, interviewReportUrl);
        }
    }
}