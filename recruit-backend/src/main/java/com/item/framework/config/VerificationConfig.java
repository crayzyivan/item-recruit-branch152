package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author : lh
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "verification.config")
public class VerificationConfig {

    // 基础配置
    private int codeLength = 6;
    // numeric/alphabetic/alphanumeric
    private String charset = "numeric";
    // 秒
    private long sendInterval = 60;

    // 安全配置
    private int maxErrorCount = 5;
    // 分钟
    private long lockDuration = 5;
    // 分钟
    private long codeExpiration = 10;

    // 获取字符集
    public String getCharsetString() {
        return switch (charset.toLowerCase()) {
            case "numeric" -> "0123456789";
            case "alphabetic" -> "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            default -> "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        };
    }
}
