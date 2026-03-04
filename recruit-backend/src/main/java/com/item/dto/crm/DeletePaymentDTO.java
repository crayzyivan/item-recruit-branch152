package com.item.dto.crm;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class DeletePaymentDTO {
    /**
     * 获取卡片接口的id
     */
    @NotNull(message = "paymentId must not be null")
    private Long paymentId;

    /**
     * 获取卡片接口返回数据中的methodId
     */
    @NotNull(message = "methodId must not be null")
    private Long methodId;
}
