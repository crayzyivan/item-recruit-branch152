package com.item.framework.annotation;

import com.item.framework.http.validator.CardValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for credit card number validation using Apache Commons Validator
 * 
 * This annotation validates credit card numbers using the Luhn algorithm and card type validation
 * 
 * @author lh
 * @since 1.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = CardValidator.class)
public @interface ValidCard {

    /**
     * Default error message
     */
    String message() default "Invalid card number";

    /**
     * Groups for validation
     */
    Class<?>[] groups() default {};

    /**
     * Payload for validation
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to allow null values
     */
    boolean allowNull() default true;

    /**
     * Whether to allow empty strings
     */
    boolean allowEmpty() default false;

    /**
     * Specific card types to validate against
     * If empty, validates against all supported card types
     */
    CardType[] cardTypes() default {};

    /**
     * Supported card types
     */
    enum CardType {
        VISA("Visa"),
        MASTERCARD("Mastercard"),
        AMEX("American Express"),
        DISCOVER("Discover"),
        JCB("JCB"),
        DINERS_CLUB("Diners Club");

        private final String displayName;

        CardType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 