package com.item.framework.annotation;

import java.lang.annotation.*;

/**
 * Web logging annotation for marking methods that need logging
 * @author hua.liu
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestLog {
    /**
     * Description of the operation
     */
    String description() default "";

    /**
     * Whether to skip logging request parameters
     */
    boolean skipRequestLog() default false;

    /**
     * Whether to skip logging response data
     */
    boolean skipResponseLog() default false;
} 