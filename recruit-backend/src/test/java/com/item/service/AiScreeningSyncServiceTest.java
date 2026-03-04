package com.item.service;

import com.item.entity.ApplicationScreeningReportsEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * ai筛选结果同步
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10  10:35
 */
@Slf4j
@SpringBootTest
public class AiScreeningSyncServiceTest {

    @Resource
    private ApplicationScreeningReportsService applicationScreeningReportsService;
    @Resource
    private AiScreeningSyncService aiScreeningSyncService;


    @Test
    public void getReportByApplicationId(){
        String applicationId = "b0255000-3f27-480b-bf5e-7305349a1e46";
        ApplicationScreeningReportsEntity reportsEntity = applicationScreeningReportsService.getReportByApplicationId(UUID.fromString(applicationId));
        log.info("Found AI screening report: {}", reportsEntity);
    }

    @Test
    public void syncScreeningResult(){
        Boolean aBoolean = aiScreeningSyncService.syncScreeningResult(780L, "b0255000-3f27-480b-bf5e-7305349a1e46");
        log.info("Sync AI screening result: {}", aBoolean);
    }



}