package com.item.vo;

import lombok.Data;

@Data
public class JobApprovalSettingsVO {
    private String companyCode;
    private Boolean approvalRequired;
    private Boolean emailNotifications;
    private String adminEmail;
}
