package com.item.framework.config;

import com.item.util.RsaUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.function.Consumer;

/**
 * RSA配置类
 * 从Nacos配置中心获取不同敏感字段的RSA密钥对
 *
 * @author lh
 */
@Data
@Slf4j
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "encrypt.function")
public class RsaConfig {

    private boolean enabled;

    // 各个敏感字段的密钥配置
    private KeyConfig password;
    private KeyConfig defaultField;

    // 密钥缓存
    private  PublicKey publicKeyPassword = null;
    private  PrivateKey privateKeyPassword = null;
    private  PublicKey publicKeyDefault = null;
    private  PrivateKey privateKeyDefault = null;

    @Data
    public static class KeyConfig {
        private boolean enabled;
        private String privateKey;
        private String publicKey;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing RSA configuration... enabled {}", enabled);
        if (!enabled) {
            return ;
        }
        // 初始化各个字段类型的密钥对
        initKeyPair(this::setPublicKeyPassword, this::setPrivateKeyPassword, password);
        initKeyPair(this::setPublicKeyDefault, this::setPrivateKeyDefault, defaultField);
    }

    /**
     * 初始化密钥对
     * @param keyConfig 密钥配置
     */
    private void initKeyPair(Consumer<PublicKey> publicKey, Consumer<PrivateKey> privateKey, KeyConfig keyConfig) {
        try {
            if (keyConfig != null && keyConfig.isEnabled() && 
                isValidKeyPair(keyConfig.getPublicKey(), keyConfig.getPrivateKey())) {
                publicKey.accept(RsaUtils.getPublicKey(keyConfig.getPublicKey()));
                privateKey.accept(RsaUtils.getPrivateKey(keyConfig.getPrivateKey()));
                log.info("Initialized RSA key keyConfig success");
            } else {
                log.warn("Invalid or missing RSA key ");
            }
        } catch (Exception e) {
            log.error("Failed to initialize RSA key ", e);
        }
    }

    /**
     * 验证密钥对是否有效
     *
     * @param publicKeyBase64  公钥Base64字符串
     * @param privateKeyBase64 私钥Base64字符串
     * @return 是否有效
     */
    private boolean isValidKeyPair(String publicKeyBase64, String privateKeyBase64) {
        return StringUtils.isNotBlank(publicKeyBase64) && StringUtils.isNotBlank(privateKeyBase64);
    }

    /**
     * 刷新密钥对缓存（当配置更新时调用）
     */
    public void refreshKeyPairs() {
        log.info("Refreshing RSA key pairs from Nacos configuration...");
        // 重新初始化密钥对
        init();
        log.info("RSA key pairs refreshed successfully. Current loaded key pairs");
    }
} 