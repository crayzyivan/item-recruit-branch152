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
@ConfigurationProperties(prefix = "iam.auth.parse")
public class IamPathConfig {
    private MethodPathConfig excluded = new MethodPathConfig();
    private MethodPathConfig simulateFrontRequest = new MethodPathConfig();
    private String tokenBase;

    @Data
    public static class MethodPathConfig {
        /**
         * 排除的路径列表
         * 支持Ant风格的路径匹配，如：/iam/login/*
         */
        private Set<String> path = new HashSet<>();

        /**
         * 排除的HTTP方法
         */
        private Set<String> method = new HashSet<>();
    }

}
