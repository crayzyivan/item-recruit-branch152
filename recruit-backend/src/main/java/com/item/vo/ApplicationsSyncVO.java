package com.item.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 同步关联表数据
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-20  15:03
 */
@Data
public class ApplicationsSyncVO {
    private String security;
    private LocalDateTime createStartTime;
    private LocalDateTime createEndTime;
    private LocalDateTime updateStartTime;
    private LocalDateTime updateEndTime;
    private List<String> applicationIds;
}