package com.item.framework.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.item.dto.CompanyInfoSimpleDTO;
import com.item.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Caffeine Cache Configuration
 * 
 * @author lh
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CaffeineConfig {

    private final CompanyService companyService;
    private final RecruitCommonNacosConfig recruitCommonNacosConfig;

    /**
     * Create LoadingCache Spring Bean for company info caching
     * 
     * Configuration:
     * - Expire after write: 60 seconds
     * - Maximum size: 300 entries
     * - Removal listener with logging
     * - Auto-reload using CompanyService.getCompanyInfoByCode when cache miss
     * 
     * @return LoadingCache for CompanyInfoSimpleDTO
     */
    @RefreshScope
    @Bean("companyInfoLoadingCache")
    public LoadingCache<String, CompanyInfoSimpleDTO> companyInfoLoadingCache() {
        // Configure removal listener with logging
        RemovalListener<String, CompanyInfoSimpleDTO> removalListener = (key, value, cause) -> {
            log.info("Company info cache removed - key: {}, cause: {}, value: {}", key, cause, value);
        };

        // Build LoadingCache with specified configuration
        LoadingCache<String, CompanyInfoSimpleDTO> loadingCache = Caffeine.newBuilder()
                // Maximum 300 entries
                .maximumSize(recruitCommonNacosConfig.getCompany().getMaximumSize())
                // Expire after 60 seconds
                .expireAfterWrite(Duration.ofSeconds(recruitCommonNacosConfig.getCompany().getExpireAfterWrite()))
                // Add removal listener
                .removalListener(removalListener)
                .build(key -> {
                    // Auto-reload logic when cache miss
                    log.info("Loading company info from service for companyCode: {}", key);
                    if (StringUtils.isBlank(key)) {
                        log.warn("CompanyCode is null, returning empty CompanyInfoSimpleDTO");
                        return new CompanyInfoSimpleDTO();
                    }
                    
                    CompanyInfoSimpleDTO companyInfo = companyService.getCompanyInfoByCode(key);
                    log.debug("Loaded company info from service for companyCode: {}, result: {}", key, companyInfo);
                    // Return empty DTO if service returns null to avoid cache pollution
                    return companyInfo != null ? companyInfo : new CompanyInfoSimpleDTO();
                });

        log.info("Company info LoadingCache initialized with maxSize={}, expireAfterWrite={}s", recruitCommonNacosConfig.getCompany().getMaximumSize(), recruitCommonNacosConfig.getCompany().getExpireAfterWrite());
        return loadingCache;
    }
}
