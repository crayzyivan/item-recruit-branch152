package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * AyrShare配置类
 * 从Nacos配置中心获取AyrShare相关配置信息
 *
 * @author hua.liu
 * @since 2025-08-27
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "ayrshare.info")
public class AyrShareConfig {

    /**
     * AyrShare API 基础url
     */
    private String baseUrl = "https://api.ayrshare.com/api";

    /**
     * 发布的接口
     */
    private String postUrl = "/post";

    /**
     * 生成jwt的接口
     */
    private String generateJwtUrl = "/profiles/generateJWT";

    /**
     * 获取用户信息的接口
     */
    private String getUserUrl = "/user";

    private String createUserProfile = "/profiles";

    private String domain;

    /**
     *
     */
    private String apiKey;

    /**
     *
     */
    private String userProfileKey;

    private String privateKey;

    /**
     * 发送模式 默认0 all 1获取一次在发送
     */
    private Integer platform = 0;

    private String subreddit = "test";

    private int imageUrlExpireSeconds = 8 * 3600;

    private String imageUrl;

    public String assemblyGenerateJwtUrl() {
        return baseUrl + generateJwtUrl;
    }

    public String assemblyGetUserUrl() {
        return baseUrl + getUserUrl;
    }

    public String assemblyCreateUserProfileUrl() {
        return baseUrl + createUserProfile;
    }
}
