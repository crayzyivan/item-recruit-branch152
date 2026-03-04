package com.item.dto.job;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 候选人职位申请批量更新DTO
 *
 * @author lh
 * @since 2025-07-07
 */
@Data
public class CandidateJobBatchUpdateDto {

    /**
     * 申请记录ID列表
     * 必填，不能为空
     */
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;

    /**
     * 申请状态
     * 必填，0:未被查看;1:已送达;2:录用;-1:拒绝
     */
    @NotNull(message = "申请状态不能为空")
    private Integer applyStatus;
} 