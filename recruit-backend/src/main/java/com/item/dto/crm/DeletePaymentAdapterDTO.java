package com.item.dto.crm;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class DeletePaymentAdapterDTO {

    private String customerCodeOrId;
    private Long paymentId;
    private Long methodId;
}
