package com.item.vo;

import lombok.Data;

/**
 * 积分记录
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-30  15:53
 */
@Data
public class PointLogVO {
    //招聘方用户id  必填
    private Long userId;
    //操作用户id
    private Long operUserId;
    //积分交易号 必填  可以使用RedisSerialNumberUtils生成
    private String transactionNo;
    //积分数 冻结、扣减时必填
    private Integer points;
    //积分交易类型 必填  TransactionTypeEnum
    private Integer transactionType;

    private String companyCode;
    //候选人职位关联is  ai面试必填
    private Long candidateJobId;
    private Long candidateId;
    private Long jobId;
    //备注
    private String remark;

    //面试时长秒数 ai面试必填(分钟计费模式)
    private Integer durationSeconds;
    private Integer durationMinutes;
    private Integer consumedTokens;
    private Integer pricingModel;

    private Integer freezeInterviewType;
    private Integer actualInterviewType;

}