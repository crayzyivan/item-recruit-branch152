package com.item.vo;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * Recommend job request view object
 * Used for single job recommendation request from controller
 *
 * @author hua.liu
 * @since 2025-10-23
 */
@Data
public class RecommendJobEmailRequestDTO {

    @NotNull(message = "Job ID cannot be null")
    private Long jobId;

    @NotNull(message = "Candidate ID cannot be null")
    private Long candidateId;

    private String recommendReasons;
}

