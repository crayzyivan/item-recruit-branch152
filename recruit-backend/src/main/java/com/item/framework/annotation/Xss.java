package com.item.framework.annotation;

import com.item.framework.http.validator.XssListValidator;
import com.item.framework.http.validator.XssValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/8/8
 * @since 1.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {XssValidator.class, XssListValidator.class})
public @interface Xss {
    // 错误提示信息
    String message() default "There are some special characters, such as \"<\", \">\", \"<a>\", etc.";

    // 分组
    Class<?>[] groups() default {};

    // 负载
    Class<? extends Payload>[] payload() default {};
}
