package com.item.framework.utils;

import static com.item.framework.constant.CommonConstants.StrConstants.TRACE_ID_HEADER;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * @author : lh
 */
public class TraceIdUtils {

    private static final String TRACE_ID_KEY = TRACE_ID_HEADER;

    /**
     * 生成16位traceId
     */
    public static String generateTraceId() {
        // traceId
        UUID uuid = UUID.randomUUID();

        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        return String.format("%016x%016x", mostSigBits, leastSigBits);
    }

    /**
     * 设置traceId到MDC
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.trim().isEmpty()) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }

    /**
     * 获取当前traceId
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 初始化新的trace（生成新的traceId和requestId）
     */
    public static String initTrace() {
        String traceId = generateTraceId();
        setTraceId(traceId);
        return traceId;
    }

    /**
     * 清理MDC - 防止内存泄漏
     */
    public static void clearTrace() {
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * 清理所有MDC - 彻底清理
     */
    public static void clearAll() {
        MDC.clear();
    }

    /**
     * 获取当前MDC的所有内容（用于线程传递）
     */
    public static java.util.Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * 设置MDC内容（用于线程传递）
     */
    public static void setContextMap(java.util.Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }
}
