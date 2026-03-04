package com.item.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 候选人招聘申请时间表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-24  14:29
 */
@Data
public class ProcessTimeLineVO {

    private String event;

    private LocalDateTime eventTime;


}