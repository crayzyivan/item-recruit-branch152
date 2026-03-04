package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 * 用于管理JWT相关的配置参数
 */
@Component
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT密钥
     */
    private String secretKey ;
    
    /**
     * token过期时间（毫秒）
     */
    private long expirationTime; // 24小时
    
    /**
     * token刷新时间（毫秒）
     *
     */
    private long refreshTime; // 1小时

    private String subject;


}