package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

/**
 * @author : lh
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "iam.common")
public class IamCommonConfig {
    private RegisterCandidateConfig registerCandidate;
    private CompanyConfig companyConfig;

    @Data
    public static class RegisterCandidateConfig {
        private String companyCode;
        private String belong2CompanyCode;
        private String source;
        private String employeeCode;
        private String applicationNote;
        private Boolean employeeVerify;
        private List<String> grantedAppCodes;
        /**
         * IAM创建用户的默认密码
         * 用于邀请面试功能中创建IAM账号
         * 如果未配置，将使用默认值 "Password123!"
         */
        private String defaultPassword = "Password123!";
    }

    @Data
    public static class CompanyConfig {
        private Set<String> switchTenantExcludeCompany;
    }

}
