package com.item.dto.job;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobAuditHistoryBO {

    private Long id;
    private Long jobId;
    private Integer oldStatus;
    private Integer newStatus;
    private String action;
    private String comment;
    private Long createdBy;
    private LocalDateTime createdAt;

}
