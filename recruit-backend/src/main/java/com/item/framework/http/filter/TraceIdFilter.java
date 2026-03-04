package com.item.framework.http.filter;

import static com.item.framework.constant.CommonConstants.StrConstants.TRACE_ID_HEADER;
import com.item.framework.utils.TraceIdUtils;
import com.item.util.LanguageLocalUtils;
import com.item.util.UserContextUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author : lh
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@RequiredArgsConstructor
public class TraceIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (StringUtils.isBlank(traceId)) {
                TraceIdUtils.initTrace();
            } else if (StringUtils.isNotBlank(traceId)) {
                TraceIdUtils.setTraceId(traceId);
            }
            //设置 language locale
            UserContextUtil.setLanguageLocal(LanguageLocalUtils.getLocale(request));
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("IamAuthenticationFilter shouldNotFilter traceId error: ", e);
        } finally {
            // 关键：清理MDC，防止内存泄漏
            TraceIdUtils.clearTrace();
            // 清理language locale
            UserContextUtil.cleanLocal();
        }
    }
}
