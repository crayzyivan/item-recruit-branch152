package com.item.service.impl;

import com.item.framework.config.RsaConfig;
import com.item.service.RsaService;
import com.item.util.RsaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * RSA服务实现类
 * 实现RSA加密解密业务逻辑
 * 
 * @author lh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RsaServiceImpl implements RsaService {
    
    private final RsaConfig rsaConfig;
    
    @Override
    public String decryptPassword(String encryptedData) {
        log.debug("Decrypting data for encryptedData: {}", encryptedData);
        if (!rsaConfig.isEnabled()) {
            log.info("Decrypting enabled {}", rsaConfig.isEnabled());
            return encryptedData;
        }
        if (!rsaConfig.getPassword().isEnabled()) {
            log.info("Decrypting getPassword enabled {}", rsaConfig.getPassword().isEnabled());
            return encryptedData;
        }

        PrivateKey privateKeyPassword = rsaConfig.getPrivateKeyPassword();
        return RsaUtils.decrypt(encryptedData, privateKeyPassword);
    }

    @Override
    public String decryptDefault(String encryptedData) {
        log.debug("Decrypting data for encryptedData: {}", encryptedData);
        if (!rsaConfig.isEnabled()) {
            log.info("Decrypting enabled {}", rsaConfig.isEnabled());
            return encryptedData;
        }
        if (!rsaConfig.getDefaultField().isEnabled()) {
            log.info("Decrypting getDefaultField enabled {}", rsaConfig.getDefaultField().isEnabled());
            return encryptedData;
        }

        PrivateKey privateKey = rsaConfig.getPrivateKeyDefault();
        return RsaUtils.decrypt(encryptedData, privateKey);
    }

    @Override
    public String encryptPassword(String decryptData) {
        log.debug("Decrypting data for decryptData: {}", decryptData);
        if (!rsaConfig.isEnabled()) {
            log.info("Decrypting enabled {}", rsaConfig.isEnabled());
            return decryptData;
        }
        if (!rsaConfig.getPassword().isEnabled()) {
            log.info("Decrypting getPassword enabled {}", rsaConfig.getPassword().isEnabled());
            return decryptData;
        }

        PublicKey publicKey= rsaConfig.getPublicKeyPassword();
        return RsaUtils.encrypt(decryptData, publicKey);
    }

    @Override
    public String encryptDefault(String decryptData) {
        log.debug("Decrypting data for decryptData: {}", decryptData);
        if (!rsaConfig.isEnabled()) {
            log.info("Decrypting enabled {}", rsaConfig.isEnabled());
            return decryptData;
        }
        if (!rsaConfig.getDefaultField().isEnabled()) {
            log.info("Decrypting getDefaultField enabled {}", rsaConfig.getDefaultField().isEnabled());
            return decryptData;
        }

        PublicKey publicKey= rsaConfig.getPublicKeyDefault();
        return RsaUtils.encrypt(decryptData, publicKey);
    }
} 