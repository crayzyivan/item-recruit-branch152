package com.item.vo;

import lombok.Builder;
import lombok.Data;

/**
 * ai面试结果查询
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:33
 */
@Data
@Builder
public class AiVettedQueryVO {
    private Integer pageIndex;
    private Integer pageSize;
    private Long jobId;
    private Integer score;
}