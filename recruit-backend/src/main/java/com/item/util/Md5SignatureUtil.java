package com.item.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 signature utility for generating and verifying MD5-based signatures.
 * Commonly used for API request signing and data integrity verification.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Generate signature
 * Map<String, String> params = new HashMap<>();
 * params.put("userId", "12345");
 * params.put("timestamp", "1234567890");
 * String signature = Md5SignatureUtil.generateSignature(params, "mySecretKey");
 *
 * // Verify signature
 * boolean isValid = Md5SignatureUtil.verifySignature(params, signature, "mySecretKey");
 * }</pre>
 *
 * @author hua.liu
 */
@Slf4j
public class Md5SignatureUtil {

    private Md5SignatureUtil() {
        // Utility class, prevent instantiation
    }

    /**
     * Generates MD5 hash for a given string.
     *
     * @param input The input string to hash
     * @return MD5 hash in lowercase hexadecimal format, or null if input is blank
     */
    public static String md5(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not available", e);
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    /**
     * Converts byte array to hexadecimal string.
     *
     * @param bytes Byte array to convert
     * @return Hexadecimal string in lowercase
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
