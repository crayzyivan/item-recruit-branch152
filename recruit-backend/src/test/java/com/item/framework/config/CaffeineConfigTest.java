package com.item.framework.config;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.item.dto.CompanyInfoSimpleDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class for CaffeineConfig
 * 
 * @author
 */
@Slf4j
@SpringBootTest
class CaffeineConfigTest {
    private static final String TEST_COMPANY_CODE = "RDXX0001";

    @Resource
    private LoadingCache<String, CompanyInfoSimpleDTO> companyInfoLoadingCache;

    @Test
    void testLoadingCacheWithValidCompanyCode() {
        CompanyInfoSimpleDTO companyInfoSimpleDTO = companyInfoLoadingCache.get(TEST_COMPANY_CODE);
        log.info("companyInfoSimpleDTO={}", companyInfoSimpleDTO);
        CompanyInfoSimpleDTO companyInfoSimpleDTO1 = companyInfoLoadingCache.get(TEST_COMPANY_CODE);
        log.info("companyInfoSimpleDTO={}", companyInfoSimpleDTO1);
    }
}
