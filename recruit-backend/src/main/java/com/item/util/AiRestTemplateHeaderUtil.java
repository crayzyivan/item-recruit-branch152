package com.item.util;

import com.item.dto.iam.IamUserContextDTO;
import com.item.entity.JobEntity;
import com.item.es.entity.JobEsEntity;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

/**
 * AI接口HTTP头配置工具类
 * 用于在调用AI接口时添加用户上下文信息到HTTP请求头中
 * 
 * @author : yarong.guo
 */
public class AiRestTemplateHeaderUtil {

    /**
     * X-Tenant-Id HTTP头名称
     */
    private static final String X_TENANT_ID = "X-Tenant-Id";
    
    /**
     * X-User-Id HTTP头名称
     */
    private static final String X_USER_ID = "X-User-Id";
    
    /**
     * X-User-Name HTTP头名称
     */
    private static final String X_USER_NAME = "X-User-Name";

    /**
     * 私有构造函数，防止实例化
     */
    private AiRestTemplateHeaderUtil() {
        // 工具类不允许实例化
    }

    /**
     * 添加用户上下文信息到HTTP请求头中
     * 当用户未登录时，不会添加任何头信息，确保向后兼容性
     * 
     */
    public static HttpHeaders addUserContextHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUser();
        if (currentUser != null) {
            // 安全地添加用户上下文头信息，避免null值
            Optional.ofNullable(currentUser.getCompanyCode())
                    .ifPresent(code -> headers.set(X_TENANT_ID, code));
            Optional.ofNullable(currentUser.getId())
                    .ifPresent(id -> headers.set(X_USER_ID, id));
            Optional.ofNullable(currentUser.getUserName())
                    .ifPresent(name -> headers.set(X_USER_NAME, name));
        }
        return headers;
    }

    public static HttpHeaders addUserContextHeadersByJob(JobEntity jobEntity) {
        HttpHeaders headers = new HttpHeaders();

        if (jobEntity != null) {
            // 安全地添加用户上下文头信息，避免null值
            Optional.ofNullable(jobEntity.getCompanyCode())
                    .ifPresent(code -> headers.set(X_TENANT_ID, code));
            Optional.ofNullable(jobEntity.getCreateBy())
                    .ifPresent(id -> headers.set(X_USER_ID, String.valueOf(id)));
            Optional.ofNullable(jobEntity.getCreateUser())
                    .ifPresent(name -> headers.set(X_USER_NAME, name));
        }
        return headers;
    }

    public static HttpHeaders addUserContextHeadersByEsJob(JobEsEntity jobEsEntity) {
        HttpHeaders headers = new HttpHeaders();

        if (jobEsEntity != null) {
            // 安全地添加用户上下文头信息，避免null值
            Optional.ofNullable(jobEsEntity.getCompanyCode())
                    .ifPresent(code -> headers.set(X_TENANT_ID, code));
            Optional.ofNullable(jobEsEntity.getCreateBy())
                    .ifPresent(id -> headers.set(X_USER_ID, String.valueOf(id)));
            Optional.ofNullable(jobEsEntity.getCreateUser())
                    .ifPresent(name -> headers.set(X_USER_NAME, name));
        }
        return headers;
    }
}
