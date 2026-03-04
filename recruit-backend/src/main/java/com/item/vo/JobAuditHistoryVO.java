package com.item.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobAuditHistoryVO {
    private Long id;
    private String auditorName;
    private String auditorId;
    private Integer fromStatus;
    private Integer toStatus;
    private LocalDateTime auditTime;
    private String comment;
    private String action;
}

