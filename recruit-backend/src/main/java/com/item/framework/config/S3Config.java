package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "aws.s3")
public class S3Config {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucketName;
    private String folder;
}
