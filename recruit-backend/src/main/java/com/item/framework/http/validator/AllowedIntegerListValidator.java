package com.item.framework.http.validator;

import com.google.common.collect.Sets;
import com.item.framework.annotation.AllowedInteger;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Validator for AllowedIntegerValues annotation
 *
 * @author lh
 * @since 1.0.0
 */
public class AllowedIntegerListValidator implements ConstraintValidator<AllowedInteger, Collection<Integer>> {

    private int[] allowedValues;
    private boolean allowNull;
    private String messageTemplate;

    @Override
    public void initialize(AllowedInteger constraintAnnotation) {
        this.allowedValues = constraintAnnotation.values();
        this.allowNull = constraintAnnotation.allowNull();
        this.messageTemplate = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Collection<Integer> value, ConstraintValidatorContext context) {
        // If value is null, check if null is allowed
        if (value == null || CollectionUtils.isEmpty(value)) {
            return allowNull;
        }

        // Check if the value is in the allowed values array
        ;
        boolean isValid = CollectionUtils.subtract(Sets.newHashSet(value), Arrays.stream(allowedValues).boxed().collect(Collectors.toSet())).isEmpty();

        if (!isValid) {
            // Disable default message
            context.disableDefaultConstraintViolation();

            // Create custom message with placeholders
            String allowedValuesStr = Arrays.stream(allowedValues)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(", "));

            String customMessage = messageTemplate
                    .replace("{values}", allowedValuesStr);

            context.buildConstraintViolationWithTemplate(customMessage)
                    .addConstraintViolation();
        }

        return isValid;
    }
}