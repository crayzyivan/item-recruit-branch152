package com.item.dto.job;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 历史职位列表请求DTO
 * 
 * @author hua.liu
 * @since 2025-08-26
 */
@Data
public class JobHistoryListRequestDTO {
    
    /**
     * 搜索关键字（职位标题）
     */
    private String keyword;
    
    /**
     * 页码，从1开始
     */
    @Min(value = 1, message = "Page index must be greater than or equal to {value}")
    private Integer pageIndex = 1;
    
    /**
     * 页大小，最大20
     */
    @Min(value = 1, message = "Page size must be greater than or equal to {value}")
    @Max(value = 20, message = "Page size must be less than or equal to {value}")
    private Integer pageSize = 20;
}
