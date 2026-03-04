package com.item.dto.iam;

import lombok.Data;

import java.util.List;

/**
 * 积分规则
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-31  15:18
 */
@Data
public class PagedResult<T> {
    private List<T> list;
    private int totalCount;
    private int currentPage;
    private int pageSize;
    private int totalPage;
}