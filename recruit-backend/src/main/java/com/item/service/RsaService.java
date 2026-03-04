package com.item.service;

/**
 * RSA服务接口
 * 提供RSA加密解密业务逻辑
 *
 * @author lh
 */
public interface RsaService {

    /**
     * 使用指定字段类型的私钥解密数据
     *
     * @param encryptedData Base64编码的加密数据
     * @return 解密后的原始数据
     */
    String decryptPassword(String encryptedData);

    String decryptDefault(String encryptedData);


    /**
     * 加密
     *
     * @param decryptData
     * @return
     */
    String encryptPassword(String decryptData);

    String encryptDefault(String decryptData);

} 