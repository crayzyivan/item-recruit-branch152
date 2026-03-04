package com.item.vo;

import com.item.framework.constant.JobApprovalAction;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class JobApprovalRequestVO {
    @NotNull
    private Long jobId;
    
    @NotNull
    private JobApprovalAction action;
    
    private String comment;
}
