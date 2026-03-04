package com.item.dto.iam;

import com.item.framework.annotation.AllowedInteger;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分交易请求DTO
 * @author : lh
 */
@Data
public class IamPointsTransactionDTO {

    /**
     * 页码，必须
     */
    @NotNull(message = "pageIndex must not be null")
    @Min(value = 1, message = "pageIndex must be greater than or equal to {value}")
    private Integer pageIndex;
    
    /**
     * 页面大小，必须
     */
    @NotNull(message = "pageSize must not be null")
    @Min(value = 1, message = "pageSize must be greater than or equal to {value}")
    @Max(value = 100, message = "pageSize must be less than or equal to {value}")
    private Integer pageSize;
    
    /**
     * 交易类型
     * Transaction type (1-Gifted, 2-Paid, 3-Consume, 4-Freeze, 5-Freeze Confirm, 6-Freeze Cancel, 7-Freeze Expire, 8-Transfer In, 9-Transfer Out, 10-Gifted Clear)
     */
    @AllowedInteger(values = {1,2,3,4,5,6,7,8,9,10}, message = "transactionType must be one of the allowed values: {values}")
    private Integer transactionType;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 交易号模糊查询
     */
    private String transactionNoLike;
}