package com.item.framework.http.validator;

import com.item.framework.annotation.StringList;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Validator for ValidatedStringList annotation
 *
 * This validator performs comprehensive validation on List<String> data:
 * 1. Validates the size of the list
 * 2. Validates the length of each string element
 * 3. Handles null values and empty elements according to configuration
 *
 * @author lh
 * @since 1.0.0
 */
@Slf4j
public class ValidatedStringListValidator implements ConstraintValidator<StringList, List<String>> {

    private int minSize;
    private int maxSize;
    private int minLength;
    private int maxLength;
    private boolean allowNull;
    private boolean allowEmpty;
    private boolean allowNullElements;
    private boolean allowEmptyElements;
    private String sizeMessage;
    private String lengthMessage;

    @Override
    public void initialize(StringList annotation) {
        this.minSize = annotation.minSize();
        this.maxSize = annotation.maxSize();
        this.minLength = annotation.minLength();
        this.maxLength = annotation.maxLength();
        this.allowNull = annotation.allowNull();
        this.allowEmpty = annotation.allowEmpty();
        this.allowNullElements = annotation.allowNullElements();
        this.allowEmptyElements = annotation.allowEmptyElements();
        this.sizeMessage = annotation.sizeMessage();
        this.lengthMessage = annotation.lengthMessage();
    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        // 1. Handle null values
        if (value == null) {
            if (!allowNull) {
                String message = sizeMessage
                        .replace("{minSize}", String.valueOf(minSize))
                        .replace("{maxSize}", String.valueOf(maxSize));
                addConstraintViolation(context, message);
                return false;
            }
            return true;
        }

        // 2. Handle empty list
        if (CollectionUtils.isEmpty(value)) {
            if (!allowEmpty && minSize > 0) {
                String message = sizeMessage
                        .replace("{minSize}", String.valueOf(minSize))
                        .replace("{maxSize}", String.valueOf(maxSize));
                addConstraintViolation(context, message);
                return false;
            }
            return true;
        }

        // 3. Validate list size
        if (!isValidSize(value.size(), context)) {
            return false;
        }

        // 4. Validate each element
        return isValidElements(value, context);
    }

    /**
     * Validates the size of the list
     */
    private boolean isValidSize(int size, ConstraintValidatorContext context) {
        if (size < minSize || size > maxSize) {
            String message = sizeMessage
                    .replace("{minSize}", String.valueOf(minSize))
                    .replace("{maxSize}", String.valueOf(maxSize));
            addConstraintViolation(context, message);
            return false;
        }
        return true;
    }

    /**
     * Validates each string element in the list
     */
    private boolean isValidElements(List<String> value, ConstraintValidatorContext context) {
        for (int i = 0; i < value.size(); i++) {
            String element = value.get(i);

            // Check for null elements
            if (element == null) {
                if (!allowNullElements) {
                    String message = lengthMessage
                            .replace("{minLength}", String.valueOf(minLength))
                            .replace("{maxLength}", String.valueOf(maxLength))
                            + " (element at index " + i + ")";
                    addConstraintViolation(context, message);
                    return false;
                }
                continue; // Skip further validation for null elements
            }

            // Check for empty string elements
            if (element.isEmpty()) {
                if (!allowEmptyElements) {
                    String message = lengthMessage
                            .replace("{minLength}", String.valueOf(minLength))
                            .replace("{maxLength}", String.valueOf(maxLength))
                            + " (element at index " + i + ")";
                    addConstraintViolation(context, message);
                    return false;
                }
                continue; // Skip length validation for empty elements
            }

            // Check element length
            if (element.length() < minLength || element.length() > maxLength) {
                String message = lengthMessage
                        .replace("{minLength}", String.valueOf(minLength))
                        .replace("{maxLength}", String.valueOf(maxLength))
                        + " (element at index " + i + ": '" + truncateString(element, 10) + "')";
                addConstraintViolation(context, message);
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a constraint violation with the specified message
     */
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }

    /**
     * Truncates a string to the specified maximum length for error message display
     */
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
