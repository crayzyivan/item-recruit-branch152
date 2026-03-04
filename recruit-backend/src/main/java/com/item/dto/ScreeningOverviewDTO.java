package com.item.dto;

import lombok.Data;

/**
 * 菲律宾ai面试结果 overview
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10  11:15
 */
@Data
public class ScreeningOverviewDTO {
    private String summary;
    private String analysis;
    private String comments;
    private String strengths;
    private String weaknesses;
}