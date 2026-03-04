package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * S3第二数据源配置类
 * 用于配置第二个S3存储服务
 * 
 * @author system
 */
@Component
@Data
@ConfigurationProperties(prefix = "aws.s3.secondary")
public class S3SecondaryConfig {
    /**
     * 访问密钥
     */
    private String accessKey;
    
    /**
     * 秘密密钥
     */
    private String secretKey;
    
    /**
     * 区域
     */
    private String region;
    
    /**
     * 存储桶名称
     */
    private String bucketName;
    
    /**
     * 文件夹前缀
     */
    private String folder;
    
    /**
     * 是否启用第二数据源
     */
    private boolean enabled = false;
}
