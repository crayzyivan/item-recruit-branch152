package com.item.dto.crm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.item.framework.config.YearMonthDeserializer;
import lombok.Data;

import java.time.YearMonth;

/**
 * <p>
 * Payment information data DTO
 * </p>
 *
 * @author lh
 */
@Data
public class PaymentInformationDataDTO {
    
    /**
     * Payment record ID
     */
    private Long id;
    
    /**
     * Payment type
     * 1: Card Payment, 3: Bank Transfer
     */
    private Integer paymentType;
    
    /**
     * Payment sub type
     * 101: Visa, 102: MasterCard, 301: ACH, 302: Wire Transfer, 303: Check
     */
    private Integer subType;
    
    /**
     * Whether this is the default payment method
     */
    private Boolean isDefault;
    
    /**
     * Payment method ID
     */
    private Long methodId;
    
    /**
     * Account holder name for bank transfer
     */
    private String accountHolderName;
    
    /**
     * Account number for bank transfer
     */
    private String accountNumber;
    
    /**
     * Card number for card payment
     */
    private String cardNumber;
    
    /**
     * Card holder name for card payment
     */
    private String cardHolderName;
    
    /**
     * Routing number for bank transfer
     */
    private String routingNumber;
    
    /**
     * Card expiry date
     */
    @JsonDeserialize(using = YearMonthDeserializer.class)
    private YearMonth expiryDate;
    
    /**
     * SWIFT code for international bank transfer
     */
    private String swiftCode;
    
    /**
     * Branch number for bank transfer
     */
    private String branchNumber;
    
    /**
     * Address line 1
     */
    private String address1;
    
    /**
     * City
     */
    private String city;
    
    /**
     * State/Province
     */
    private String state;
    
    /**
     * Country
     */
    private String country;
    
    /**
     * CVV code for card payment
     */
    private String cvv;
    
    /**
     * ZIP/Postal code
     */
    private String zipCode;
    
    /**
     * Address line 2
     */
    private String address2;
    
    /**
     * Street address
     */
    private String streetAddress;
    
    /**
     * Credit card type
     * 0: Unknown, 1: Visa, 2: MasterCard, 3: American Express, 4: Discover
     */
    private Integer creditCardType;
}