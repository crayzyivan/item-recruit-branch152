package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Boot configuration properties for encryption scenarios.
 * Binds YAML configuration under "recruit.encryption" prefix.
 * Supports Nacos dynamic configuration refresh.
 *
 * Example YAML configuration:
 * <pre>
 * recruit:
 *   encryption:
 *     scenarios:
 *       - name: job-share
 *         prime: 77777777
 *         checksum-length: 5
 *         hmac-key: "YourBase64EncodedKeyHere=="
 * </pre>
 *
 * @author hua.liu
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "recruit.encryption")
public class EncryptionConfigProperties {

    private List<ScenarioConfig> scenarios = new ArrayList<>();

    /**
     * Configuration for a single encryption scenario.
     */
    @Data
    public static class ScenarioConfig {
        private String name;
        private Long prime;
        private Integer checksumLength;
        private String hmacKey; // Base64-encoded HMAC key
    }
}
