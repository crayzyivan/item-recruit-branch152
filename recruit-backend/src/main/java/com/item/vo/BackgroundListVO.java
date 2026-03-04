package com.item.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 背景调查列表信息
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-23  10:58
 */
@Data
public class BackgroundListVO {
    private Long id;
    private Long candidateJobId;
    private Long candidateId;
    private String candidateName;
    private String firstName;
    private String lastName;
    private String middleName;
    private Long jobId;
    private String jobTitle;
    private String email;
    private LocalDateTime applicationDate;
    private int backgroundStatus;
    private String reportUrl;


}