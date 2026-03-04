package com.item.dto.background;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p>
 * 用户背调结果类(为Recruit系统展示)
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
@Data
public class UserBackgroundCheckDTO implements Serializable {
    private Long id;
    private Long candidateId;
    private Long jobId;
    private String candidateName;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private LocalDateTime applicationDate;
    private LocalDateTime backgroundDate;
    private LocalDateTime expires;
    private String reportStage;
    private String reportUrl;
}
