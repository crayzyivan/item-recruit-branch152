package com.item.util;

import com.item.framework.constant.CommonResponseCode;
import com.item.framework.error.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加密解密工具类
 * 提供RSA非对称加密解密功能
 * 
 * @author lh
 */
@Slf4j
public class RsaUtils {
    
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    // 2048位RSA密钥的最大加密块大小
    private static final int MAX_ENCRYPT_BLOCK_SIZE = 245;
    // 2048位RSA密钥的最大解密块大小
    private static final int MAX_DECRYPT_BLOCK_SIZE = 256;
    
    private RsaUtils() {
        // 工具类，禁止实例化
    }
    
    /**
     * 使用公钥加密数据
     * 
     * @param data 待加密的数据
     * @param publicKeyBase64 Base64编码的公钥
     * @return Base64编码的加密结果
     */
    public static String encrypt(String data, String publicKeyBase64) {
        if (StringUtils.isBlank(data)) {
            log.warn("Data to encrypt is blank");
            return data;
        }
        
        if (StringUtils.isBlank(publicKeyBase64)) {
            log.error("Public key is blank");
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
        
        try {
            PublicKey publicKey = getPublicKey(publicKeyBase64);
            return encrypt(data, publicKey);
        } catch (Exception e) {
            log.error("Failed to encrypt data with public key", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_ENCRYPT_FAIL);
        }
    }
    
    /**
     * 使用公钥加密数据
     * 
     * @param data 待加密的数据
     * @param publicKey 公钥对象
     * @return Base64编码的加密结果
     */
    public static String encrypt(String data, PublicKey publicKey) {
        if (StringUtils.isBlank(data)) {
            log.warn("Data to encrypt is blank");
            return data;
        }
        
        if (publicKey == null) {
            log.error("Public key is null");
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
        
        try {
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(dataBytes);
            
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_ENCRYPT_FAIL);
        }
    }
    
    /**
     * 使用私钥解密数据
     * 
     * @param encryptedData Base64编码的加密数据
     * @param privateKeyBase64 Base64编码的私钥
     * @return 解密后的原始数据
     */
    public static String decrypt(String encryptedData, String privateKeyBase64) {
        if (StringUtils.isBlank(encryptedData)) {
            log.warn("Encrypted data is blank");
            return encryptedData;
        }
        
        if (StringUtils.isBlank(privateKeyBase64)) {
            log.error("Private key is blank");
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
        
        try {
            PrivateKey privateKey = getPrivateKey(privateKeyBase64);
            return decrypt(encryptedData, privateKey);
        } catch (Exception e) {
            log.error("Failed to decrypt data with private key", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_DECRYPT_FAIL);
        }
    }
    
    /**
     * 使用私钥解密数据
     * 
     * @param encryptedData Base64编码的加密数据
     * @param privateKey 私钥对象
     * @return 解密后的原始数据
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey) {
        if (StringUtils.isBlank(encryptedData)) {
            log.warn("Encrypted data is blank");
            return encryptedData;
        }
        
        if (privateKey == null) {
            log.error("Private key is null");
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
        
        try {
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt data", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_DECRYPT_FAIL);
        }
    }
    
    /**
     * 分段加密长数据
     * 
     * @param data 待加密的数据
     * @param publicKeyBase64 Base64编码的公钥
     * @return Base64编码的加密结果
     */
    public static String encryptLongData(String data, String publicKeyBase64) {
        if (StringUtils.isBlank(data)) {
            log.warn("Data to encrypt is blank");
            return data;
        }
        
        if (StringUtils.isBlank(publicKeyBase64)) {
            log.error("Public key is blank");
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
        
        try {
            PublicKey publicKey = getPublicKey(publicKeyBase64);
            return encryptLongData(data, publicKey);
        } catch (Exception e) {
            log.error("Failed to encrypt long data with public key", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_ENCRYPT_FAIL);
        }
    }
    
    /**
     * 分段加密长数据
     * 
     * @param data 待加密的数据
     * @param publicKey 公钥对象
     * @return Base64编码的加密结果
     */
    public static String encryptLongData(String data, PublicKey publicKey) {
        if (StringUtils.isBlank(data)) {
            log.warn("Data to encrypt is blank");
            return data;
        }
        
        if (publicKey == null) {
            log.error("Public key is null");
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
        
        try {
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            
            // 如果数据长度小于等于最大加密块大小，直接加密
            if (dataBytes.length <= MAX_ENCRYPT_BLOCK_SIZE) {
                byte[] encryptedBytes = cipher.doFinal(dataBytes);
                return Base64.getEncoder().encodeToString(encryptedBytes);
            }
            
            // 分段加密
            StringBuilder result = new StringBuilder();
            int offset = 0;
            while (offset < dataBytes.length) {
                int blockSize = Math.min(MAX_ENCRYPT_BLOCK_SIZE, dataBytes.length - offset);
                byte[] block = new byte[blockSize];
                System.arraycopy(dataBytes, offset, block, 0, blockSize);
                
                byte[] encryptedBlock = cipher.doFinal(block);
                result.append(Base64.getEncoder().encodeToString(encryptedBlock));
                
                offset += blockSize;
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Failed to encrypt long data", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_ENCRYPT_FAIL);
        }
    }
    
    /**
     * 分段解密长数据
     * 
     * @param encryptedData Base64编码的加密数据
     * @param privateKeyBase64 Base64编码的私钥
     * @return 解密后的原始数据
     */
    public static String decryptLongData(String encryptedData, String privateKeyBase64) {
        if (StringUtils.isBlank(encryptedData)) {
            log.warn("Encrypted data is blank");
            return encryptedData;
        }
        
        if (StringUtils.isBlank(privateKeyBase64)) {
            log.error("Private key is blank");
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
        
        try {
            PrivateKey privateKey = getPrivateKey(privateKeyBase64);
            return decryptLongData(encryptedData, privateKey);
        } catch (Exception e) {
            log.error("Failed to decrypt long data with private key", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_DECRYPT_FAIL);
        }
    }
    
    /**
     * 分段解密长数据
     * 
     * @param encryptedData Base64编码的加密数据
     * @param privateKey 私钥对象
     * @return 解密后的原始数据
     */
    public static String decryptLongData(String encryptedData, PrivateKey privateKey) {
        if (StringUtils.isBlank(encryptedData)) {
            log.warn("Encrypted data is blank");
            return encryptedData;
        }
        
        if (privateKey == null) {
            log.error("Private key is null");
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
        
        try {
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            
            // 如果加密数据长度小于等于最大解密块大小，直接解密
            if (encryptedBytes.length <= MAX_DECRYPT_BLOCK_SIZE) {
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            }
            
            // 分段解密
            StringBuilder result = new StringBuilder();
            int offset = 0;
            while (offset < encryptedBytes.length) {
                int blockSize = Math.min(MAX_DECRYPT_BLOCK_SIZE, encryptedBytes.length - offset);
                byte[] block = new byte[blockSize];
                System.arraycopy(encryptedBytes, offset, block, 0, blockSize);
                
                byte[] decryptedBlock = cipher.doFinal(block);
                result.append(new String(decryptedBlock, StandardCharsets.UTF_8));
                
                offset += blockSize;
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Failed to decrypt long data", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_DECRYPT_FAIL);
        }
    }
    
    /**
     * 从Base64编码的公钥字符串获取公钥对象
     * 
     * @param publicKeyBase64 Base64编码的公钥
     * @return 公钥对象
     */
    public static PublicKey getPublicKey(String publicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("Failed to parse public key", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
    }
    
    /**
     * 从Base64编码的私钥字符串获取私钥对象
     * 
     * @param privateKeyBase64 Base64编码的私钥
     * @return 私钥对象
     */
    public static PrivateKey getPrivateKey(String privateKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("Failed to parse private key", e);
            throw BusinessException.of(CommonResponseCode.COMMON_RSA_INVALID_KEY);
        }
    }
    
    /**
     * 验证公钥格式是否正确
     * 
     * @param publicKeyBase64 Base64编码的公钥
     * @return 是否有效
     */
    public static boolean isValidPublicKey(String publicKeyBase64) {
        try {
            getPublicKey(publicKeyBase64);
            return true;
        } catch (Exception e) {
            log.warn("Invalid public key format: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证私钥格式是否正确
     * 
     * @param privateKeyBase64 Base64编码的私钥
     * @return 是否有效
     */
    public static boolean isValidPrivateKey(String privateKeyBase64) {
        try {
            getPrivateKey(privateKeyBase64);
            return true;
        } catch (Exception e) {
            log.warn("Invalid private key format: {}", e.getMessage());
            return false;
        }
    }
} 