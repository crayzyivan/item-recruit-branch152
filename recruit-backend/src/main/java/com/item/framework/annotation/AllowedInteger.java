package com.item.framework.annotation;

import com.item.framework.http.validator.AllowedIntegerListValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import com.item.framework.http.validator.AllowedIntegerValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for checking if an Integer value is in the allowed values list
 *
 * @author lh
 * @since 1.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {AllowedIntegerValidator.class, AllowedIntegerListValidator.class})
public @interface AllowedInteger {

    /**
     * Default error message with placeholder support
     * {0} - field name
     * {1} - allowed values
     * {2} - actual value
     */
    String message() default "must be one of the allowed values: {values}";

    /**
     * Groups for validation
     */
    Class<?>[] groups() default {};

    /**
     * Payload for validation
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Allowed integer values
     */
    int[] values();

    /**
     * Whether to allow null values
     */
    boolean allowNull() default true;
}