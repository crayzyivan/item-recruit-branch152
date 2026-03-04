package com.item.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 用于生成32字节（256位）HMAC-SHA256密钥，Base64编码输出
 */
public class SecureHmacKeyGenerator {
    public static String generateSecureHmac256Key() {
        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (Exception e) {
            // 兼容性兜底
            secureRandom = new SecureRandom();
        }
        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);
        return Base64.getEncoder().withoutPadding().encodeToString(keyBytes);
    }

    /**
     * 生成密钥
     */
//    public static void main(String[] args) {
//        String key = generateSecureHmac256Key();
//        System.out.println("HMAC-SHA256密钥（Base64）：" + key);
//    }
} 