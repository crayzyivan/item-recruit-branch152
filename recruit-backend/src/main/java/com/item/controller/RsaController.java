package com.item.controller;

import com.item.service.RsaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RSA控制器
 * 提供RSA加密解密相关的API接口
 * 
 * @author lh
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rsa")
@RequiredArgsConstructor
public class RsaController {
    
    private final RsaService rsaService;
    
    /**
     * 获取指定字段类型的公钥
     *
     * @return 公钥信息
     */
//    @GetMapping("/password/encrypt")
//    public String getPublicKey(@RequestParam String password) {
//        log.info("Getting public key for field type: {}", password);
//
//        String publicKey = rsaService.encryptPassword(password);
//
//
//        return publicKey;
//    }
//    /**
//     * 解密数据
//     *
//     * @param request 解密请求
//     * @return 解密结果
//     */
//    @GetMapping("/decrypt")
//    public String decrypt(@RequestParam String request) {
//        log.info("Decrypting data for field type: {}", request);
//
//        String decryptedData = rsaService.decryptPassword(request);
//
//
//        return decryptedData;
//    }

} 