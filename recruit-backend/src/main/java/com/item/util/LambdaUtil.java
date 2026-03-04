package com.item.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author : lh
 */
@Slf4j
public class LambdaUtil {
    // 缓存字段名，提高性能
    private static final Map<String, String> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取属性名
     */
    public static <T> String getFieldName(CFunction<T, ?> fn) {
        // 获取lambda表达式的key
        String key = fn.getClass().getName();
        // 先从缓存中获取
        return FIELD_CACHE.computeIfAbsent(key, k -> {
            try {
                Method method = fn.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(fn);
                String methodName = serializedLambda.getImplMethodName();

                // 获取字段名
                String fieldName = methodToProperty(methodName);

                return fieldName;
            } catch (Exception e) {
                log.error("LambdaUtil getFieldName error", e);
                throw new RuntimeException("获取属性名失败", e);
            }
        });
    }

    /**
     * 将方法名转换为属性名
     */
    private static String methodToProperty(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            methodName = methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            methodName = methodName.substring(2);
        } else {
            return methodName;
        }

        // 首字母小写
        return Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1);
    }

    /**
     * 获取完整类名
     */
    public static <T> String getClassName(CFunction<T, ?> fn) {
        try {
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(fn);
            return serializedLambda.getImplClass().replace("/", ".");
        } catch (Exception e) {
            log.error("LambdaUtil getClassName error", e);
            throw new RuntimeException("获取类名失败", e);
        }
    }

    /**
     * 批量获取属性名
     */
    @SafeVarargs
    public static <T> String[] getFieldNames(CFunction<T, ?>... fns) {
        String[] fieldNames = new String[fns.length];
        for (int i = 0; i < fns.length; i++) {
            fieldNames[i] = getFieldName(fns[i]);
        }
        return fieldNames;
    }

    @FunctionalInterface
    public interface CFunction<T, R> extends Function<T, R>, java.io.Serializable {
    }
}
