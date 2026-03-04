package com.item.framework.http.validator;

import com.item.framework.annotation.ValidCard;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator for ValidCreditCard annotation using Apache Commons Validator
 * 
 * @author lh
 * @since 1.0.0
 */
public class CardValidator implements ConstraintValidator<ValidCard, String> {

    private static final org.apache.commons.validator.routines.CreditCardValidator COMMONS_VALIDATOR = 
        new org.apache.commons.validator.routines.CreditCardValidator();

    private boolean allowNull;
    private boolean allowEmpty;
    private Set<String> allowedCardTypes;
    private String messageTemplate;

    @Override
    public void initialize(ValidCard constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.messageTemplate = constraintAnnotation.message();
        
        // Convert card types to set of strings for easy lookup
        this.allowedCardTypes = Arrays.stream(constraintAnnotation.cardTypes())
                .map(ValidCard.CardType::getDisplayName)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Handle null values
        if (value == null) {
            return allowNull;
        }

        // Handle empty strings
        if (StringUtils.isBlank(value)) {
            return allowEmpty;
        }

        // Clean the card number (remove spaces, dashes, etc.)
        String cleanCardNumber = value.replaceAll("[^0-9]", "");

        // Basic length validation
        if (cleanCardNumber.length() < 13 || cleanCardNumber.length() > 19) {
            setCustomMessage(context, messageTemplate);
            return false;
        }

        // Validate using Apache Commons Validator
        if (!COMMONS_VALIDATOR.isValid(cleanCardNumber)) {
            setCustomMessage(context, messageTemplate);
            return false;
        }

        // If specific card types are specified, validate against them
//        if (!allowedCardTypes.isEmpty()) {
//            String detectedCardType = detectCardType(cleanCardNumber);
//            if (!allowedCardTypes.contains(detectedCardType)) {
//                setCustomMessage(context, "Card type not allowed. Allowed types: " +
//                    String.join(", ", allowedCardTypes));
//                return false;
//            }
//        }

        return true;
    }

    /**
     * Detect card type based on card number prefix
     * 
     * @param cardNumber the card number
     * @return detected card type
     */
    private String detectCardType(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return "Visa";
        } else if (cardNumber.startsWith("5") && 
                   cardNumber.charAt(1) >= '1' && cardNumber.charAt(1) <= '5') {
            return "Mastercard";
        } else if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
            return "American Express";
        } else if (cardNumber.startsWith("6011") || 
                   (cardNumber.startsWith("622") && 
                    Integer.parseInt(cardNumber.substring(3, 6)) >= 126 && 
                    Integer.parseInt(cardNumber.substring(3, 6)) <= 925) ||
                   (cardNumber.startsWith("64") && 
                    cardNumber.charAt(2) >= '4' && cardNumber.charAt(2) <= '9') ||
                   cardNumber.startsWith("65")) {
            return "Discover";
        } else if (cardNumber.startsWith("35")) {
            return "JCB";
        } else if ((cardNumber.startsWith("30") && 
                    cardNumber.charAt(2) >= '0' && cardNumber.charAt(2) <= '5') ||
                   cardNumber.startsWith("36") || 
                   cardNumber.startsWith("38") || 
                   cardNumber.startsWith("39")) {
            return "Diners Club";
        }
        
        return "Unknown";
    }

    /**
     * Set custom error message
     * 
     * @param context the constraint validator context
     * @param message the error message
     */
    private void setCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
} 