package com.item.dto.iam;

import lombok.Data;

@Data
public class SubscriptionTrialDTO {
    private Long id;
    private String appCode;
    private String companyCode;
    private String companyName;
    private String planCode;
    private String planName;
    private Integer subscriptionType;
    private Integer chargeByTime;
    private String subscriptionStatus;
    private String subscriptionStartTime;
    private String periodEnd;
    private String customerCode;
    private String remark;
    private String createdAt;
    private String subscriptionEndTime;
}

