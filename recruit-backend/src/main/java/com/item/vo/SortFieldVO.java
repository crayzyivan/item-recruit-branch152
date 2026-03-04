package com.item.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 排序字段
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-22  09:54
 */
@Data
@AllArgsConstructor
public class SortFieldVO {
    /**
     * 排序字段名称
     */
    @NotBlank(message = "Sort field name cannot be empty")
    private String fieldName;

    /**
     * 排序方向：ASC 或 DESC
     */
    @Pattern(regexp = "^(ASC|DESC)$", message = "Sort order must be ASC or DESC")
    private String sortOrder = "DESC";
}