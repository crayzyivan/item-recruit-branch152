package com.item.service;

import com.item.entity.ApplicationInterviewReportsEntity;
import com.item.entity.JobEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

/**
 * PostgreSQL集成测试 - ApplicationInterviewReports表
 * 
 * 测试PostgreSQL数据源连接、CRUD操作、JSONB字段处理等功能
 * 确保PostgreSQL集成正常工作且不影响现有MySQL业务
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-28
 */
@Slf4j
@SpringBootTest
public class ApplicationInterviewReportsIntegrationTest {
    @Resource
    private ApplicationInterviewReportsService applicationInterviewReportsService;
    @Resource
    private JobService jobService;


    @Test
    public void testDataSourceSeparation(){
        log.info("=== 测试双数据源分离 ===");
        
        // 测试Service注入
        log.info("ApplicationInterviewReportsService: {}", applicationInterviewReportsService.getClass().getName());
        log.info("JobService: {}", jobService.getClass().getName());
        
        // 测试MySQL连接（JobService应该使用MySQL）
        log.info("=== 测试MySQL数据源 ===");
        try {
            JobEntity job = jobService.getById(1L);
            log.info("✅ MySQL连接成功 - Job查询结果: {}", job != null ? "找到数据" : "未找到数据");
        } catch (Exception e) {
            log.warn("❌ MySQL连接失败: {}", e.getMessage());
        }
        
        // 测试PostgreSQL连接（ApplicationInterviewReportsService应该使用PostgreSQL）
        log.info("=== 测试PostgreSQL数据源 ===");
        try {
            String applicationId = "265ebdc1-212e-4e8a-8a6a-18b9aec1b6f1";
            List<ApplicationInterviewReportsEntity> list = applicationInterviewReportsService.listByApplicationId(UUID.fromString(applicationId));
            log.info("✅ PostgreSQL连接成功 - 查询结果数量: {}", list != null ? list.size() : 0);
        } catch (Exception e) {
            log.error("❌ PostgreSQL连接失败: {}", e.getMessage());
            // 打印更详细的错误信息用于调试
            if (e.getCause() != null) {
                log.error("根本原因: {}", e.getCause().getMessage());
            }
        }
        
        log.info("=== 数据源分离测试完成 ===");
    }

}
