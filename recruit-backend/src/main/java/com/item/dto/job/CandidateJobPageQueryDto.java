package com.item.dto.job;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 候选人职位关联表分页查询DTO
 *
 * @author lh
 * @since 2025-07-07
 */
@Data
public class CandidateJobPageQueryDto {

    /**
     * 当前页
     * 必填，必须大于0，默认值1
     */
    @NotNull(message = "当前页不能为空")
    @Min(value = 1, message = "当前页必须大于0")
    private Long current = 1L;

    /**
     * 每页大小
     * 必填，必须大于0，默认值10
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    private Long size = 10L;

    /**
     * 候选人ID（可选）
     */
    private Long candidateId;

    /**
     * 职位ID（可选）
     */
    private Long jobId;

    /**
     * 申请状态（可选）
     */
    private Integer applyStatus;

    /**
     * 发布状态（可选）
     */
    private Integer posted;
} 