package com.item.util;

import com.item.framework.constant.CommonResponseCode;
import com.item.framework.error.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * RSA密钥生成工具类
 * 用于生成不同敏感字段的独立密钥对
 * 
 * @author lh
 */
@Slf4j
public class RsaKeyGenerator {
    /**
     * RSA密钥对信息
     */
    public static class RsaKeyPair {
        private final String publicKey;
        private final String privateKey;
        
        public RsaKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
        
        public String getPublicKey() {
            return publicKey;
        }
        
        public String getPrivateKey() {
            return privateKey;
        }

        
        @Override
        public String toString() {
            return String.format("RsaKeyPair{publicKey='%s...', privateKey='%s...'}",
                    publicKey.substring(0, Math.min(20, publicKey.length())),
                    privateKey.substring(0, Math.min(20, privateKey.length())));
        }
    }
    
    private static final int RSA_KEY_SIZE = 2048;
    private static final String RSA_ALGORITHM = "RSA";
    
    private RsaKeyGenerator() {
        // 工具类，禁止实例化
    }
    
    /**
     * 生成RSA密钥对
     *
     * @return RSA密钥对信息
     */
    public static RsaKeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            SecureRandom secureRandom = new SecureRandom();
            keyPairGenerator.initialize(RSA_KEY_SIZE, secureRandom);
            
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            
            RsaKeyPair rsaKeyPair = new RsaKeyPair(publicKey, privateKey);
            
            log.info("Generated RSA key pair rsaKeyPair: {}", rsaKeyPair);
            return rsaKeyPair;
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate RSA key pair ", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_KEY_GENERATE_FAIL);
        }
    }

    /**
     * 打印密钥对信息（用于开发调试）
     * 
     * @param keyPair RSA密钥对
     */
    public static void printKeyPair(RsaKeyPair keyPair) {
        System.out.println("Public Key: " + keyPair.getPublicKey());
        System.out.println("Private Key: " + keyPair.getPrivateKey());
        System.out.println("==========================================");
    }
    
    /**
     * 主方法，用于独立运行生成密钥对
     * 使用方法：java -cp . RsaKeyGenerator
     */
    public static void main(String[] args) {
        try {
            log.info("Starting RSA key pair generation...");
            
            // 生成所有敏感字段的密钥对
            for (int i = 0; i < 3; i++) {
                RsaKeyPair rsaKeyPair = generateKeyPair();
                printKeyPair(rsaKeyPair);
            }
            
            log.info("RSA key pair generation completed successfully.");
            
        } catch (Exception e) {
            log.error("Failed to generate RSA key pairs", e);
            System.exit(1);
        }
    }
} 