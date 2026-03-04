package com.item.dto.crm;

import com.item.framework.annotation.AllowedInteger;
import com.item.framework.annotation.ValidCard;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.YearMonth;

/**
 * @author : lh
 */
@Data
public class UpdatePaymentDTO {
    /**
     * Payment record ID
     * Example: 0
     */
    @NotNull(message = "id must not be null")
    private Long id;

    /**
     * Payment type required
     * Example: 1
     * 1:CardPayment, Card payment;   3:BankTransfer, Bank Transfer;
     * Allowed values: 1 3
     */
    @NotNull(message = "paymentType must not be null")
    @AllowedInteger(values = {1, 3}, message = "paymentType must be one of the allowed values: {values}")
    private Integer paymentType;

    /**
     * Payment sub type required
     * Example: 101
     * 101:CreditCard; 102:DebitCard; 301:Ach, ACH; 302:Wire;  303:Check;
     * Allowed values: 101 102 301 302 303
     */
    @NotNull(message = "subType must not be null")
    @AllowedInteger(values = {101, 102, 301, 302, 303}, message = "subType must be one of the allowed values: {values}")
    private Integer subType;

    /**
     * Whether this is the default payment method
     * Example: true
     */
    private Boolean isDefault;

    /**
     * Card number required
     * Example: string
     *
     * Card Number
     * >= 1 characters
     * Match pattern:
     * ^\d{15,19}$
     */
    @NotBlank(message = "cardNumber must not be blank")
    @Pattern(regexp = "^\\d{15,19}$", message = "cardNumber must be 15-19 digits")
    @ValidCard(message = "cardNumber Invalid card number")
    private String cardNumber;

    /**
     * Card expiry date yyyy-MM required
     * Example: Expiry Date
     */
    private YearMonth expiryDate;

    /**
     * Card holder name required
     * Example: string
     * >= 0 characters <= 50 characters
     */
    @NotBlank(message = "cardHolderName must not be blank")
    @Size(min = 1, max = 50, message = "cardHolderName size must be between {min} and {max}")
    private String cardHolderName;

    /**
     * Card CVV required
     * Example: string
     * >= 0 characters  <= 10 characters
     */
    @NotBlank(message = "cvv must not be blank")
    @Size(min = 1, max = 10, message = "cvv size must be between {min} and {max}")
    private String cvv;

    /**
     * Billing address line 1
     * Example: string
     * >= 0 characters <= 200 characters
     */
//    @NotBlank(message = "address1 must not be blank")
    @Size(min = 0, max = 200, message = "address1 size must be between {min} and {max}")
    private String address1;

    /**
     * Billing address line 2
     * Example: string
     * >= 0 characters <= 200 characters
     */
//    @NotBlank(message = "address2 must not be blank")
    @Size(min = 0, max = 200, message = "address2 size must be between {min} and {max}")
    private String address2;

    /**
     * City
     * Example: string
     * >= 0 characters <= 200 characters
     */
    @Size(min = 0, max = 200, message = "address2 size must be between {min} and {max}")
    private String city;

    /**
     * State/Province
     * Example: string
     * >= 0 characters <= 200 characters
     */
    @Size(min = 0, max = 200, message = "state size must be between {min} and {max}")
    private String state;

    /**
     * Country
     * Example: string
     *  >= 0 characters <= 200 characters
     */
    @Size(min = 0, max = 200, message = "country size must be between {min} and {max}")
    private String country;

    /**
     * ZIP/Postal code
     * Example: string
     * >= 0 characters <= 20 characters
     */
    @NotBlank(message = "zipCode must not be blank")
    @Size(min = 1, max = 20, message = "zipCode size must be between {min} and {max}")
    private String zipCode;


    @NotNull(message = "methodId must not be null")
    private Long methodId;
}
