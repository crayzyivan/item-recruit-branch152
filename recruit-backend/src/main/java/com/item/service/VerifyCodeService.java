package com.item.service;

/**
 * @author : lh
 */
public interface VerifyCodeService {
    void sendVerificationCode(String email, String type);
    boolean verifyCode(String email, String code, String type);
    void handleFailedAttempt(String email, String type, String codeKey);
}
