package com.item.service.impl;

import com.item.service.AyrShareTokenDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * AyrShareTokenDomainServiceImpl allowHotListSaveConfig method unit tests
 * 
 * @author test
 * @since 2025-09-08
 */
@Slf4j
@SpringBootTest
class AyrShareTokenDomainServiceImplTest extends BaseServiceTestWithUserContextAyrShare {
    @Resource
    private AyrShareTokenDomainService ayrShareTokenDomainService;

    @Test
    void allowHotListSaveConfig() {
        Boolean hotListSaveConfig = ayrShareTokenDomainService.allowHotListSaveConfig();
        log.info("hotListSaveConfig: {}", hotListSaveConfig);
    }
}
