package com.item.service.client;

import com.item.service.client.adapter.IamRpcAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Small wrapper to expose commonly used IAM lookups in one place.
 * Keeps IAM-related error handling consistent across services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamUserClient {

    private final IamRpcAdapter iamRpcAdapter;

    /**
     * Resolve a user's email by id. Returns null when not found or on error.
     */
    public String getUserEmailById(Long userId) {
        try {
            if (userId == null) {
                return null;
            }
            var user = iamRpcAdapter.getUserInfo(userId);
            return user != null ? user.getEmail() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch user by id {}", userId, e);
            return null;
        }
    }

     public String getAdminEmailByCompanyCode(String companyCode) {
        try {
            var admin = iamRpcAdapter.getAdminByCompanyCode(companyCode);
            return admin != null ? admin.getEmail() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch admin by companyCode {}", companyCode, e);
            return null;
        }
    }
}