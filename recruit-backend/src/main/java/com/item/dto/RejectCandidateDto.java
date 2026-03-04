package com.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 招聘拒绝候选人状态
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-23  18:48
 */
@Data
public class RejectCandidateDto {

    @NotNull(message = "ID cannot be null")
    private Long id;

}