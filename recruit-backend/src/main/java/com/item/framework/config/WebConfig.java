package com.item.framework.config;

import com.item.framework.interceptor.AuthInterceptor;
import com.item.framework.interceptor.IpWhitelistInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/11
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final IpWhitelistInterceptor ipWhitelistInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");

        // IP 白名单拦截器，仅拦截特定路径
        registry.addInterceptor(ipWhitelistInterceptor)
                .addPathPatterns("/api/interview/**");
    }

}