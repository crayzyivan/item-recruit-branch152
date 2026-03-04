package com.item.service.impl;

import com.item.framework.config.EncryptionConfig;
import com.item.framework.config.EncryptionConfigProperties;
import com.item.framework.constant.CandidateResponseCode;
import com.item.framework.error.BusinessException;
import com.item.service.ShortIdGenerator;
import com.item.service.encryption.EncryptionConfigRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Spring Bean: Short ID generator with HMAC-SHA256 tamper-proof capability
 * 混淆方式：与大素数进行亦或（XOR）
 *
 * @author hua.liu
 */
@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class ShortIdGeneratorImpl implements ShortIdGenerator {

    // 7位十进制大素数，用于混淆
    private static final long PRIME_CONFUSION = 9999991L;

    // 校验位长度
    private static final int CHECKSUM_LENGTH = 4;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    // 从配置或环境变量注入密钥（Base64编码）
    @Value("${recruit.short.id.hmac-key:}")
    private String base64Key;

    private byte[] hmacKeyBytes;

    // Candidate share encryption constants (different from existing ones)
    private static final long SHARE_PRIME = 33333331L;
    private static final int SHARE_CANDIDATE_CHECKSUM_LEN = 6;
    private static final String SHARE_HMAC_ALGORITHM = "HmacSHA256";

    // Configuration for candidate share HMAC key
    @Value("${recruit.short.candidate.id.hmac-key:}")
    private String shareCryptKey;

    private byte[] shareCryptKeyBytes;

    // Dynamic encryption configuration support
    private final EncryptionConfigProperties encryptionConfigProperties;

    private EncryptionConfigRegistry configRegistry;

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(base64Key)) {
            throw new IllegalStateException("ShortId HMAC key must be configured (app.short-id.hmac-key)");
        }
        hmacKeyBytes = Base64.getDecoder().decode(base64Key);
        if (hmacKeyBytes.length != 32) {
            throw new IllegalArgumentException("ShortId HMAC key must be 32 bytes (Base64-encoded)");
        }
    }

    @PostConstruct
    public void initShareKey() {
        if (StringUtils.isBlank(shareCryptKey)) {
            log.warn("Candidate share HMAC key is not configured (recruit.candidate.share.hmac-key)");
            // Do not throw exception, allow system to start, but will check when calling encryption method
            return;
        }

        try {
            shareCryptKeyBytes = Base64.getDecoder().decode(shareCryptKey);

            if (shareCryptKeyBytes.length != 32) {
                throw new IllegalArgumentException(
                    "Candidate share HMAC key must be 32 bytes (256 bits), got: "
                    + shareCryptKeyBytes.length + " bytes");
            }

            log.info("Candidate share HMAC key initialized successfully");
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode candidate share HMAC key", e);
            throw new IllegalStateException(
                "Invalid candidate share HMAC key configuration", e);
        }
    }

    @PostConstruct
    public void initDynamicConfigs() {
        configRegistry = new EncryptionConfigRegistry();

        // If no dynamic configuration is provided, just initialize empty registry
        if (encryptionConfigProperties == null || 
            encryptionConfigProperties.getScenarios() == null ||
            encryptionConfigProperties.getScenarios().isEmpty()) {
            log.info("No dynamic encryption scenarios configured");
            return;
        }

        // Parse and register each scenario
        for (EncryptionConfigProperties.ScenarioConfig scenarioConfig : 
             encryptionConfigProperties.getScenarios()) {
            try {
                // Validate required fields
                if (StringUtils.isBlank(scenarioConfig.getName())) {
                    throw new IllegalStateException(
                        "Scenario name is required in encryption configuration");
                }
                if (scenarioConfig.getPrime() == null) {
                    throw new IllegalStateException(
                        String.format("Prime is required for scenario '%s'", 
                            scenarioConfig.getName()));
                }
                if (scenarioConfig.getChecksumLength() == null) {
                    throw new IllegalStateException(
                        String.format("Checksum length is required for scenario '%s'", 
                            scenarioConfig.getName()));
                }
                if (StringUtils.isBlank(scenarioConfig.getHmacKey())) {
                    throw new IllegalStateException(
                        String.format("HMAC key is required for scenario '%s'", 
                            scenarioConfig.getName()));
                }

                // Decode Base64 HMAC key
                byte[] hmacKeyBytes;
                try {
                    hmacKeyBytes = Base64.getDecoder().decode(scenarioConfig.getHmacKey());
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException(
                        String.format("Invalid Base64 HMAC key for scenario '%s': %s", 
                            scenarioConfig.getName(), e.getMessage()), e);
                }

                // Create EncryptionConfig (validation happens in constructor)
                EncryptionConfig config = new EncryptionConfig(
                    scenarioConfig.getName(),
                    scenarioConfig.getPrime(),
                    scenarioConfig.getChecksumLength(),
                    hmacKeyBytes
                );

                // Register in registry (duplicate check happens here)
                configRegistry.register(config);

                log.debug("Registered encryption scenario: {}", config);

            } catch (Exception e) {
                log.error("Failed to load encryption scenario '{}': {}", 
                    scenarioConfig.getName(), e.getMessage());
                throw new IllegalStateException(
                    String.format("Invalid encryption configuration for scenario '%s': %s",
                        scenarioConfig.getName(), e.getMessage()), e);
            }
        }

        log.info("Loaded {} dynamic encryption scenarios: {}", 
            configRegistry.size(), configRegistry.getScenarioNames());
    }

    @Override
    public String generateShortId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
        // 1. 混淆：与大素数亦或
        long confusedValue = id ^ PRIME_CONFUSION;
        // 2. 转换为 Base36
        String base36String = Long.toString(confusedValue, 36).toLowerCase();
        // 3. 生成 HMAC-SHA256 签名
        String signature = generateHmacSignature(base36String);
        // 4. 提取校验位
        String checksum = signature.substring(0, CHECKSUM_LENGTH);
        // 5. 拼接
        return checksum + base36String;
    }

    @Override
    public Long parseShortId(String shortId) {
        if (StringUtils.isBlank(shortId) || shortId.length() < CHECKSUM_LENGTH + 1) {
            log.warn("shortId length exception shortId {}", shortId);
            return null;
        }
        String checksum = shortId.substring(0, CHECKSUM_LENGTH);
        String base36String = shortId.substring(CHECKSUM_LENGTH);
        if (!isValidBase36(base36String)) {
            log.warn("Invalid Base36 format {}", shortId);
            return null;
        }
        String expectedSignature = generateHmacSignature(base36String);
        String expectedChecksum = expectedSignature.substring(0, CHECKSUM_LENGTH);
        if (!checksum.equalsIgnoreCase(expectedChecksum)) {
            log.warn("Short ID has been tampered with {}", shortId);
            return null;
        }
        long confusedValue = Long.parseLong(base36String, 36);
        // 反混淆：再亦或一次
        long originalId = confusedValue ^ PRIME_CONFUSION;
        if (originalId <= 0) {
            log.warn("Invalid ID value {}", shortId);
            return null;
        }
        return originalId;
    }

    /**
     * Encrypts an ID using the provided configuration.
     */
    private String encryptWithConfig(Long id, EncryptionConfig config) {
        // Step 1: XOR obfuscation
        long obfuscatedValue = id ^ config.getPrime();

        // Step 2: Base36 encoding
        String base36String = Long.toString(obfuscatedValue, 36).toLowerCase();

        // Step 3: Generate HMAC signature
        String signature = generateHmacSignatureWithKey(
            base36String, 
            config.getHmacKeyBytes(), 
            config.getHmacAlgorithm()
        );

        // Step 4: Extract checksum
        String checksum = signature.substring(0, config.getChecksumLength());

        // Step 5: Concatenate checksum + base36String
        return checksum + base36String;
    }

    /**
     * Decrypts an encrypted ID using the provided configuration.
     */
    private Long decryptWithConfig(String encryptedId, EncryptionConfig config) {
        try {
            // Step 1: Extract checksum and base36String
            String checksum = encryptedId.substring(0, config.getChecksumLength());
            String base36String = encryptedId.substring(config.getChecksumLength());

            // Step 2: Validate Base36 format
            if (!isValidBase36(base36String)) {
                log.warn("Invalid Base36 format in encrypted ID: {}", encryptedId);
                return null;
            }

            // Step 3: Generate expected HMAC signature
            String expectedSignature = generateHmacSignatureWithKey(
                base36String,
                config.getHmacKeyBytes(),
                config.getHmacAlgorithm()
            );
            String expectedChecksum = expectedSignature.substring(0, config.getChecksumLength());

            // Step 4: Compare checksums (case-insensitive)
            if (!checksum.equalsIgnoreCase(expectedChecksum)) {
                log.warn("Checksum mismatch for encrypted ID '{}' - possible tampering", 
                    encryptedId);
                return null;
            }

            // Step 5: Parse base36String to long
            long obfuscatedValue = Long.parseLong(base36String, 36);

            // Step 6: Reverse XOR obfuscation
            long originalId = obfuscatedValue ^ config.getPrime();

            // Step 7: Validate originalId is positive
            if (originalId <= 0) {
                log.warn("Decrypted ID is not positive for encrypted ID: {}", encryptedId);
                return null;
            }

            return originalId;

        } catch (NumberFormatException e) {
            log.warn("Failed to parse Base36 string in encrypted ID: {}", encryptedId, e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during decryption of ID: {}", encryptedId, e);
            return null;
        }
    }

    /**
     * Generates HMAC signature using the specified key and algorithm.
     */
    private String generateHmacSignatureWithKey(String input, byte[] keyBytes, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, algorithm);
            mac.init(keySpec);

            byte[] hmacBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(
                String.format("HMAC signature generation failed with algorithm '%s': %s",
                    algorithm, e.getMessage()), e);
        }
    }

    @Override
    public boolean isValidShortId(String shortId) {
        try {
            parseShortId(shortId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateHmacSignature(String input) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(hmacKeyBytes, HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA256 error", e);
        }
    }

    private boolean isValidBase36(String base36String) {
        if (StringUtils.isBlank(base36String)) {
            return false;
        }
        try {
            Long.parseLong(base36String, 36);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String encryptCandidateId(Long candidateId) {
        // 1. Validate input
        if (candidateId == null || candidateId <= 0) {
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
        }

        // 2. Check if share key is configured
        if (shareCryptKeyBytes == null) {
            throw new IllegalStateException(
                "Candidate share encryption key is not configured");
        }

        // 3. XOR obfuscation with SHARE_PRIME
        long obfuscatedValue = candidateId ^ SHARE_PRIME;

        // 4. Convert to Base36 string (lowercase)
        String base36String = Long.toString(obfuscatedValue, 36).toLowerCase();

        // 5. Generate HMAC-SHA256 signature using shareCryptKeyBytes
        String signature = generateShareHmacSignature(base36String);

        // 6. Extract SHARE_CANDIDATE_CHECKSUM_LEN characters as checksum
        String checksum = signature.substring(0, SHARE_CANDIDATE_CHECKSUM_LEN);

        // 7. Concatenate checksum + base36String
        String encryptedId = checksum + base36String;

        log.debug("Encrypted candidate ID: {} -> {}", candidateId, encryptedId);

        return encryptedId;
    }

    @Override
    public Long decryptCandidateId(String encryptedId) {
        // 1. Validate input length
        if (StringUtils.isBlank(encryptedId) ||
            encryptedId.length() < SHARE_CANDIDATE_CHECKSUM_LEN + 1) {
            log.warn("Invalid encrypted candidate ID length: {}", encryptedId);
            return null;
        }

        // 2. Check if share key is configured
        if (shareCryptKeyBytes == null) {
            log.error("Candidate share decryption key is not configured");
            return null;
        }

        try {
            // 3. Extract checksum and base36 string
            String checksum = encryptedId.substring(0, SHARE_CANDIDATE_CHECKSUM_LEN);
            String base36String = encryptedId.substring(SHARE_CANDIDATE_CHECKSUM_LEN);

            // 4. Validate Base36 format
            if (!isValidBase36(base36String)) {
                log.warn("Invalid Base36 format in encrypted ID: {}", encryptedId);
                return null;
            }

            // 5. Generate expected HMAC signature
            String expectedSignature = generateShareHmacSignature(base36String);
            String expectedChecksum = expectedSignature.substring(0, SHARE_CANDIDATE_CHECKSUM_LEN);

            // 6. Verify checksum (case-insensitive)
            if (!checksum.equalsIgnoreCase(expectedChecksum)) {
                log.warn("Checksum mismatch - encrypted ID may have been tampered: {}",
                    encryptedId);
                return null;
            }

            // 7. Parse Base36 to long
            long obfuscatedValue = Long.parseLong(base36String, 36);

            // 8. Reverse XOR obfuscation
            long originalId = obfuscatedValue ^ SHARE_PRIME;

            // 9. Validate original ID is positive
            if (originalId <= 0) {
                log.warn("Decrypted candidate ID is not positive: {}", encryptedId);
                return null;
            }

            log.debug("Decrypted candidate ID: {} -> {}", encryptedId, originalId);

            return originalId;

        } catch (NumberFormatException e) {
            log.warn("Failed to parse Base36 string: {}", encryptedId, e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during decryption: {}", encryptedId, e);
            return null;
        }
    }

    private String generateShareHmacSignature(String input) {
        try {
            Mac mac = Mac.getInstance(SHARE_HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(shareCryptKeyBytes, SHARE_HMAC_ALGORITHM);
            mac.init(keySpec);

            byte[] hmacBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA256 signature generation failed", e);
        }
    }

    @Override
    public String encrypt(Long id, String scenarioName) {
        // Validate ID
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                String.format("ID must be positive, got: %s", id));
        }

        // Validate scenario name
        if (StringUtils.isBlank(scenarioName)) {
            throw new IllegalArgumentException("Scenario name must not be blank");
        }

        // Get configuration from registry
        if (!configRegistry.contains(scenarioName)) {
            throw new IllegalArgumentException(
                String.format("Scenario '%s' not found. Available scenarios: %s",
                    scenarioName, configRegistry.getScenarioNames()));
        }

        EncryptionConfig config = configRegistry.get(scenarioName);

        // Encrypt using the configuration
        String encryptedId = encryptWithConfig(id, config);

        log.debug("Encrypted ID {} with scenario '{}': {}", id, scenarioName, encryptedId);

        return encryptedId;
    }

    @Override
    public Long decrypt(String encryptedId, String scenarioName) {
        // Validate encrypted ID
        if (StringUtils.isBlank(encryptedId)) {
            log.warn("Encrypted ID is blank");
            return null;
        }

        // Validate scenario name
        if (StringUtils.isBlank(scenarioName)) {
            throw new IllegalArgumentException("Scenario name must not be blank");
        }

        // Get configuration from registry
        if (!configRegistry.contains(scenarioName)) {
            throw new IllegalArgumentException(
                String.format("Scenario '%s' not found. Available scenarios: %s",
                    scenarioName, configRegistry.getScenarioNames()));
        }

        EncryptionConfig config = configRegistry.get(scenarioName);

        // Validate minimum length
        if (encryptedId.length() < config.getChecksumLength() + 1) {
            log.warn("Encrypted ID '{}' is too short for scenario '{}'", 
                encryptedId, scenarioName);
            return null;
        }

        // Decrypt using the configuration
        Long originalId = decryptWithConfig(encryptedId, config);

        if (originalId != null) {
            log.debug("Decrypted ID '{}' with scenario '{}': {}", 
                encryptedId, scenarioName, originalId);
        }

        return originalId;
    }
} 