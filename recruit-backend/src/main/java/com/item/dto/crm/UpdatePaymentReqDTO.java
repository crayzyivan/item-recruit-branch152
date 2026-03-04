package com.item.dto.crm;

import lombok.Data;

import java.time.YearMonth;

/**
 * @author : lh
 */
@Data
public class UpdatePaymentReqDTO {
//        "id": 0,
//            "paymentType": 1,
//            "subType": 101,
//            "isDefault": true,
//            "cardNumber": "string",
//            "expiryDate": "2019-08-24T14:15:22Z",
//            "cardHolderName": "string",
//            "cvv": "string",
//            "address1": "string",
//            "address2": "string",
//            "city": "string",
//            "state": "string",
//            "country": "string",
//            "zipCode": "string"

    /**
     * Payment record ID
     * Example: 0
     */
    private Long id;

    /**
     * Payment type required
     * Example: 1
     * 1:CardPayment, Card payment;   3:BankTransfer, Bank Transfer;
     * Allowed values: 1 3
     */
    private Integer paymentType;

    /**
     * Payment sub type required
     * Example: 101
     * 101:CreditCard; 102:DebitCard; 301:Ach, ACH; 302:Wire;  303:Check;
     * Allowed values: 101 102 301 302 303
     */
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
    private String cardNumber;

    /**
     * Card expiry date required
     * Example: Expiry Date
     */
    private YearMonth expiryDate;

    /**
     * Card holder name required
     * Example: string
     * >= 0 characters <= 50 characters
     */
    private String cardHolderName;

    /**
     * Card CVV required
     * Example: string
     * >= 0 characters  <= 10 characters
     */
    private String cvv;

    /**
     * Billing address line 1
     * Example: string
     * >= 0 characters <= 200 characters
     */
    private String address1;

    /**
     * Billing address line 2
     * Example: string
     * >= 0 characters <= 200 characters
     */
    private String address2;

    /**
     * City
     * Example: string
     * >= 0 characters <= 200 characters
     */
    private String city;

    /**
     * State/Province
     * Example: string
     * >= 0 characters <= 200 characters
     */
    private String state;

    /**
     * Country
     * Example: string
     *  >= 0 characters <= 200 characters
     */
    private String country;

    /**
     * ZIP/Postal code
     * Example: string
     * >= 0 characters <= 20 characters
     */
    private String zipCode;
}
