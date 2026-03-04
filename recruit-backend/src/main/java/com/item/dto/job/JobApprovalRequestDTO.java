package com.item.dto.job;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class JobApprovalRequestDTO {
    @NotNull
    private Long jobId;
    
    @NotNull
    private String action;
    
    private String comment;
}
