package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * @author : lh
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "recruit.common")
public class RecruitCommonNacosConfig {
    private boolean globalLogHeader = true;
    private boolean globalLogRequest = true;
    private boolean globalLogResponse = true;
    private boolean globalTraceId = true;

    private String rdControllerSecurity = "rdc@inner!168";

    //默认1天
    private Integer cacheCompanySeconds = 24 * 60 * 60;
    private Integer cacheCompanySecondsRandom = 60 * 60;

    //0 默认 companyName - jobTitle ； 1 jobTitle
    private Integer urlCodeStyle = 0;

    private LocalCacheConfig company = new LocalCacheConfig();

    // Set of company codes that should be identified as candidates
    private CandidateUserIdentify candidateUserIdentify = new CandidateUserIdentify();

    private boolean duplicateJobLocationsEffect = false;

    private int recommendCandidateCoundTimeOut = 10;

    private String candidateAnswerQuestion5sRoute;

    // 答题链接有效期（小时），如果为空或小于等于0则不校验
    private Integer answerQuestion5sLinkValidHours;

    private Set<String> excludeCompanyCodeInRecommendCandidates;

    @Data
    public static class LocalCacheConfig {
        //单位秒
        private Integer expireAfterWrite = 60;

        private Integer maximumSize = 300;
    }

    @Data
    public static class CandidateUserIdentify {
        // candidate company codes
        private Set<String> candidateCompanyCodes = new HashSet<>();

        private boolean candidatePrimaryUserEnable = true;
    }
}
