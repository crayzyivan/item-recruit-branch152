package com.item.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class JobOptionDTO {
    @Min(value = 1, message = "pageIndex must be greater than or equal to {value}")
    private Integer pageIndex;
    @Min(value = 1, message = "pageSize must be greater than or equal to {value}")
    private Integer pageSize;
}
