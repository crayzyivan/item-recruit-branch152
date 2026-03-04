package com.item.dto.job;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 应聘者职位状态更新DTO
 */
@Data
public class CandidateJobUpdateStatusDto {
    
    /**
     * 主键ID
     */
    @NotNull(message = "ID cannot be null")
    private Long id;

    /**
     * 申请状态
     */
    @NotNull(message = "Status cannot be null")
    private Integer status;

    /**
     * 原因说明（非必须）
     */
    @Size(max = 100, message = "Reason length must not exceed 500 characters")
    private String reason;
} 