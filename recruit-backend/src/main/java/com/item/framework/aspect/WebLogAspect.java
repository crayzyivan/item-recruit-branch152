package com.item.framework.aspect;

import com.item.framework.annotation.RequestLog;
import com.item.framework.config.RecruitCommonNacosConfig;
import com.item.framework.constant.CommonConstants;
import com.item.util.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Web logging aspect for controller methods
 * @author hua.liu
 */
@Aspect
@Component
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class WebLogAspect {

    private final RecruitCommonNacosConfig commonNacosConfig;

    @Pointcut("execution(* com.item.controller..*.*(..))")
    public void requestLog() {
    }

    @Around("requestLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Get request context
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // Get method annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequestLog requestLog = method.getAnnotation(RequestLog.class);

        // Log request if not skipped
        if (commonNacosConfig.isGlobalLogRequest() && isRequestLog(requestLog)) {
            // Get request parameters
            Map<String, Object> requestParams = new HashMap<>();
            Object[] args = joinPoint.getArgs();
            Parameter[] parameters = method.getParameters();
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Object value = args[i];
                
                // Skip null values
                if (value == null) {
                    continue;
                }

                // Handle file uploads
                if (value instanceof MultipartFile) {
                    requestParams.put(parameter.getName(), "<<FILE_UPLOAD>>");
                    continue;
                }

                // Skip HttpServletRequest
                if (value instanceof HttpServletRequest) {
                    requestParams.put(parameter.getName(), "<<HttpServletRequest>>");
                    continue;
                }

                if (value instanceof HttpServletResponse) {
                    requestParams.put(parameter.getName(), "<<HttpServletResponse>>");
                    continue;
                }

                // Handle @RequestBody parameters
                if (parameter.isAnnotationPresent(RequestBody.class)) {
                    requestParams.put(parameter.getName(), value);
                    continue;
                }

                // Handle other parameters
                requestParams.put(parameter.getName(), value);
            }
            if (requestLog!= null && StringUtils.isNotBlank(requestLog.description())) {
                requestParams.put("description", requestLog.description());
            }

            String headers = getHeaders(request);

            // Log request information
            log.info("request url: {} method: {} parameters: {} headers: {}", request.getRequestURL(), request.getMethod(), JsonUtils.toJson(requestParams), headers);
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            log.error("request processing failed", e);
            throw e;
        } finally {
            // Log response if not skipped
            if (commonNacosConfig.isGlobalLogResponse() && isResponse(requestLog)) {
                long executionTime = System.currentTimeMillis() - startTime;
                log.info("response [{}ms]: {}", executionTime, JsonUtils.toJson(result));
            }
        }
    }

    private static boolean isResponse(RequestLog requestLog) {
        return requestLog == null || !requestLog.skipResponseLog();
    }

    private static boolean isRequestLog(RequestLog requestLog) {
        return requestLog == null || !requestLog.skipRequestLog();
    }

    private String getHeaders(HttpServletRequest request) {
        boolean globalLogHeader = commonNacosConfig.isGlobalLogHeader();
        if (!globalLogHeader) {
            return null;
        }
        //只获取关键的信息 token等 不循环获取请求头
        return CommonConstants.StrConstants.AUTHORIZATION_HEADER + " - " + request.getHeader(CommonConstants.StrConstants.AUTHORIZATION_HEADER) + " " +
                CommonConstants.StrConstants.REFRESH_TOKEN_HEADER + " - " + request.getHeader(CommonConstants.StrConstants.REFRESH_TOKEN_HEADER) + " " +
                CommonConstants.StrConstants.NEW_REFRESH_TOKEN_HEADER + " - " + request.getHeader(CommonConstants.StrConstants.NEW_REFRESH_TOKEN_HEADER) + " " +
                CommonConstants.StrConstants.NEW_TOKEN_HEADER + " - " + request.getHeader(CommonConstants.StrConstants.NEW_TOKEN_HEADER);
    }
} 