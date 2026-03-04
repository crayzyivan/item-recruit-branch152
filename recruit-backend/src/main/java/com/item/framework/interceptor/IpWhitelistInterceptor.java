package com.item.framework.interceptor;

import com.item.framework.config.IpWhitelistProperties;
import com.item.framework.constant.AuthResponseCode;
import com.item.framework.error.BusinessException;
import com.item.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * IP 白名单拦截器
 *
 * @author Item Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpWhitelistInterceptor implements HandlerInterceptor {

    private final IpWhitelistProperties ipWhitelistProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws BusinessException {
        if (!ipWhitelistProperties.isEnabled()) {
            return true;
        }

        String clientIp = getClientIp(request);
        List<String> whitelistIps = ipWhitelistProperties.getIps();

        if (!IpUtil.isIpAllowed(clientIp, whitelistIps)) {
            log.warn("Access denied for IP: {}, Request Path: {}", clientIp, request.getRequestURI());
            throw BusinessException.of(AuthResponseCode.PERMISSION_DENIED);
        }

        return true;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headerNames = new String[] {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
                continue;
            }
            // 可能是 "ip1, ip2, ip3" 的形式，取第一个非空、非 unknown 的
            String[] ipArray = ip.split(",");
            for (String item : ipArray) {
                String candidate = item.trim();
                if (StringUtils.hasText(candidate) && !"unknown".equalsIgnoreCase(candidate)) {
                    log.info("Ip Whitelist extract headerName: {},  client IP: {}", header, candidate);
                    return candidate;
                }
            }
        }

        // 回退到 remoteAddr
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "";
    }

}
