package com.item.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.item.dto.iam.IamCreateUserReqDTO;
import com.item.entity.CandidateEntity;
import com.item.framework.config.RecruitCommonNacosConfig;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import com.item.service.CandidateDataMigrationService;
import com.item.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 应聘者数据迁移控制器
 * 提供数据迁移相关的API接口
 *
 * @author system
 */
@RestController
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequestMapping("/api/migration")
@RequiredArgsConstructor
@Slf4j
public class CandidateDataMigrationController {

    private final CandidateDataMigrationService candidateDataMigrationService;
    private final RecruitCommonNacosConfig commonNacosConfig;
    // 常量定义
    private static final String MIGRATION_FAILED_MSG = "迁移失败: ";
    private static final String ACCESS_DENIED_MSG = "访问被拒绝：安全验证失败";
    private final CandidateService candidateService;

    private boolean isAccess(String security) {
        String rdControllerSecurity = commonNacosConfig.getRdControllerSecurity();
        if (StringUtils.isBlank(security) || StringUtils.isNotBlank(security) && !Strings.CS.equals(rdControllerSecurity, security)) {
            log.warn("RDInnerController updateJobLocation rdControllerSecurity {} security {}", rdControllerSecurity, security);
            return false;
        }
        return true;
    }
    /**
     * 迁移所有应聘者数据（使用默认线程池）
     */
    @PostMapping("/candidates/all")
    public String migrateAllCandidates(@RequestParam String security) {
        if (!isAccess(security)) {
            return ACCESS_DENIED_MSG;
        }
        try {
            log.info("开始迁移所有应聘者数据");
            candidateDataMigrationService.migrateAllCandidates();
            return "所有应聘者数据迁移完成";
        } catch (Exception e) {
            log.error("迁移所有应聘者数据失败", e);
            throw new BusinessException(GlobalStatusCode.FAIL,MIGRATION_FAILED_MSG + e.getMessage());
        }
    }

    /**
     * 迁移所有应聘者数据（使用指定线程数）
     */
    @PostMapping("/candidates/all/{threadPoolSize}")
    public String migrateAllCandidatesWithThreads(@PathVariable int threadPoolSize, @RequestParam String security) {
        if (!isAccess(security)) {
            return ACCESS_DENIED_MSG;
        }
        try {
            log.info("开始迁移所有应聘者数据，线程数: {}", threadPoolSize);
            candidateDataMigrationService.migrateAllCandidates(threadPoolSize);
            return "所有应聘者数据迁移完成，使用线程数: " + threadPoolSize;
        } catch (Exception e) {
            log.error("迁移所有应聘者数据失败，线程数: {}", threadPoolSize, e);
            throw new BusinessException(GlobalStatusCode.FAIL,MIGRATION_FAILED_MSG + e.getMessage());
        }
    }

    /**
     * 批量迁移应聘者数据（使用默认线程池）
     */
    @PostMapping("/candidates/batch")
    public String migrateCandidatesBatch(
            @RequestParam int batchSize,
            @RequestParam int offset,
            @RequestParam String security) {
        if (!isAccess(security)) {
            return ACCESS_DENIED_MSG;
        }
        try {
            log.info("开始批量迁移应聘者数据，批次大小: {}, 偏移量: {}", batchSize, offset);
            candidateDataMigrationService.migrateCandidatesBatch(batchSize, offset);
            return "批量迁移完成，批次大小: " + batchSize + ", 偏移量: " + offset;
        } catch (Exception e) {
            log.error("批量迁移应聘者数据失败，批次大小: {}, 偏移量: {}", batchSize, offset, e);
            throw new BusinessException(GlobalStatusCode.FAIL,MIGRATION_FAILED_MSG + e.getMessage());
        }
    }

    /**
     * 批量迁移应聘者数据（使用指定线程数）
     */
    @PostMapping("/candidates/batch/{threadPoolSize}")
    public String migrateCandidatesBatchWithThreads(
            @RequestParam int batchSize,
            @RequestParam int offset,
            @PathVariable int threadPoolSize,
            @RequestParam String security) {
        if (!isAccess(security)) {
            return ACCESS_DENIED_MSG;
        }
        try {
            log.info("开始批量迁移应聘者数据，批次大小: {}, 偏移量: {}, 线程数: {}", 
                    batchSize, offset, threadPoolSize);
            candidateDataMigrationService.migrateCandidatesBatch(batchSize, offset, threadPoolSize);
            return "批量迁移完成，批次大小: " + batchSize + 
                    ", 偏移量: " + offset + ", 线程数: " + threadPoolSize;
        } catch (Exception e) {
            log.error("批量迁移应聘者数据失败，批次大小: {}, 偏移量: {}, 线程数: {}", 
                    batchSize, offset, threadPoolSize, e);
            throw new BusinessException(GlobalStatusCode.FAIL,MIGRATION_FAILED_MSG + e.getMessage());
        }
    }

    /**
     * 迁移指定应聘者数据
     */
    @PostMapping("/candidates/{candidateId}")
    public String migrateCandidateById(@PathVariable String candidateId, @RequestParam String security) {
        if (!isAccess(security)) {
            return ACCESS_DENIED_MSG;
        }
        try {
            log.info("开始迁移指定应聘者数据，ID: {}", candidateId);
            candidateDataMigrationService.migrateCandidatesbyCandidateId(candidateId);
            return "应聘者迁移完成，ID: " + candidateId;
        } catch (Exception e) {
            log.error("迁移指定应聘者数据失败，ID: {}", candidateId, e);
            throw new BusinessException(GlobalStatusCode.FAIL,MIGRATION_FAILED_MSG + e.getMessage());
        }
    }

    /**
     * 注册应聘者到IAM系统
     */
    @PostMapping("/candidates/register")
    public boolean registerCandidate(@RequestBody IamCreateUserReqDTO request, @RequestParam String security) {
        if (!isAccess(security)) {
            throw new BusinessException(GlobalStatusCode.FAIL,ACCESS_DENIED_MSG);
        }
        try {
            log.info("开始注册应聘者到IAM系统，邮箱: {}", request.getEmail());
            CandidateEntity candidateEntity = candidateDataMigrationService.registerCandidate(request);
            LambdaUpdateWrapper<CandidateEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CandidateEntity::getCandidatePermanentEmail, candidateEntity.getCandidateEmail());
            updateWrapper.set(CandidateEntity::getCandidateId, candidateEntity.getCandidateId());
            return candidateService.update(updateWrapper);
        } catch (Exception e) {
            log.error("注册应聘者到IAM系统失败，邮箱: {}", request.getEmail(), e);
            throw new BusinessException(GlobalStatusCode.FAIL,MIGRATION_FAILED_MSG + e.getMessage());
        }
    }

    /**
     * 高性能迁移（使用更多线程）
     */
    @PostMapping("/candidates/high-performance")
    public String highPerformanceMigration(
            @RequestParam(defaultValue = "20") int threadPoolSize,
            @RequestParam String security) {
        if (!isAccess(security)) {
            return ACCESS_DENIED_MSG;
        }
        try {
            log.info("开始高性能迁移，线程数: {}", threadPoolSize);
            candidateDataMigrationService.migrateAllCandidates(threadPoolSize);
            return "高性能迁移完成，使用线程数: " + threadPoolSize;
        } catch (Exception e) {
            log.error("高性能迁移失败，线程数: {}", threadPoolSize, e);
            throw new BusinessException(GlobalStatusCode.FAIL,MIGRATION_FAILED_MSG + e.getMessage());
        }
    }

}
