package com.item.dto.iam;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分交易请求DTO
 * @author : lh
 */
@Data
public class IamPointsTransactionReqDTO {

    /**
     * 页码，必须
     */
    private Integer pageNum;
    
    /**
     * 页面大小，必须
     */
    private Integer pageSize;
    
    /**
     * 公司代码，必须
     */
    private String companyCode;
    
    /**
     * 应用代码，必须
     */
    private String appCode;
    
    /**
     * 交易类型
     */
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