package com.item.framework.config;

import static com.item.framework.constant.CommonConstants.StrConstants.CRM_CLIENT_HEADERS_X_TENANT_ID;
import static com.item.framework.constant.CommonConstants.StrConstants.TRACE_ID_HEADER;
import com.item.framework.utils.TraceIdUtils;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;

import java.util.Set;

/**
 * @author : lh
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CrmFeignClientConfig {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private final CrmInfoConfig crmInfoConfig;

    static {
        // 配置路径匹配器
        // 不区分大小写
        PATH_MATCHER.setCaseSensitive(false);
        // 去除空格
        PATH_MATCHER.setTrimTokens(true);
        // 设置路径分隔符
        PATH_MATCHER.setPathSeparator("/");

    }

    @Bean
    public RequestInterceptor crmClientHeaderInterceptor() {
        return template -> {
            // 从当前线程的MDC中获取traceId
            String traceId = TraceIdUtils.getTraceId();
            // 如果存在traceId，添加到请求头中
            if (StringUtils.isNotBlank(traceId)) {
                template.header(TRACE_ID_HEADER, traceId);
            }
            //如果时这个crm请求 添加x-tenant-id请求头
            if (isCrmPath(template.path())) {
                // rpc的请求添加请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set(CRM_CLIENT_HEADERS_X_TENANT_ID, crmInfoConfig.getXTenantId());
                headers.forEach(template::header);
            }
        };
    }

    @Bean
    public ErrorDecoder crmErrorDecoder() {
        return new CrmFeignErrorDecoder();
    }

    /**
     * 检查路径是否Crm
     */
    private boolean isCrmPath(String path) {
        Set<String> pathConfig = crmInfoConfig.getCrm().getPath();
        for (String pathTemp : pathConfig) {
            // 检查路径匹配
            if (PATH_MATCHER.match(pathTemp, path)) {
                return true;
            }
        }
        return false;
    }

}