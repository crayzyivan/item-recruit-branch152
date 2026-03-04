package com.item.vo;

/**
 * 下载简历参数
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-30  10:43
 */

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResumeDownloadVO {
    private Long userId;
    @NotNull(message = "resumeUrl is required")
    private String resumeUrl;
}