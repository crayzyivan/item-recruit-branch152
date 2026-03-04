package com.item.framework.annotation;

import com.item.framework.http.validator.ValidatedStringListValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for validating List<String> data
 *
 * This annotation validates both the size of the list and the length of each string element.
 * It provides comprehensive validation similar to @Size annotation but specifically designed for string lists.
 *
 * @author lh
 * @since 1.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidatedStringListValidator.class)
public @interface StringList {

    /**
     * Default error message
     */
    String message() default "List validation failed";

    /**
     * Groups for validation
     */
    Class<?>[] groups() default {};

    /**
     * Payload for validation
     */
    Class<? extends Payload>[] payload() default {};

    // === List size validation ===

    /**
     * Minimum size of the list
     */
    int minSize() default 0;

    /**
     * Maximum size of the list
     */
    int maxSize() default Integer.MAX_VALUE;

    // === Element length validation ===

    /**
     * Minimum length of each string element
     */
    int minLength() default 0;

    /**
     * Maximum length of each string element
     */
    int maxLength() default Integer.MAX_VALUE;

    // === Additional configurations ===

    /**
     * Whether to allow null values for the list itself
     */
    boolean allowNull() default true;

    /**
     * Whether to allow empty list
     */
    boolean allowEmpty() default true;

    /**
     * Whether to allow null elements in the list
     */
    boolean allowNullElements() default false;

    /**
     * Whether to allow empty string elements in the list
     */
    boolean allowEmptyElements() default false;

    /**
     * Custom error message for list size validation
     */
    String sizeMessage() default "List size must be between {minSize} and {maxSize}";

    /**
     * Custom error message for element length validation
     */
    String lengthMessage() default "Element length must be between {minLength} and {maxLength}";
}
