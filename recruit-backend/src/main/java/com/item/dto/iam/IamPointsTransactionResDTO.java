package com.item.dto.iam;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分交易响应DTO
 * @author : lh
 */
@Data
public class IamPointsTransactionResDTO {
    
    /**
     * 交易号
     */
    private String transactionNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 应用代码
     */
    private String appCode;
    
    /**
     * 赠送积分变化
     */
    private Integer deltaGifted;
    
    /**
     * 付费积分变化
     */
    private Integer deltaPaid;
    
    /**
     * 冻结积分变化
     */
    private Integer deltaFrozen;
    
    /**
     * 最终赠送积分
     */
    private Integer finalGifted;
    
    /**
     * 最终付费积分
     */
    private Integer finalPaid;
    
    /**
     * 最终冻结积分
     */
    private Integer finalFrozen;
    
    /**
     * 交易类型
     */
    private Integer transactionType;
    
    /**
     * 操作者
     */
    private String operator;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}