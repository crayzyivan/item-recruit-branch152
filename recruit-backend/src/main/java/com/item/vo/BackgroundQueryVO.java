package com.item.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 背景调查列表查询
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-23  15:24
 */
@Data
@Builder
public class BackgroundQueryVO {
    private Integer pageIndex;
    private Integer pageSize;
    private Long jobId;

}