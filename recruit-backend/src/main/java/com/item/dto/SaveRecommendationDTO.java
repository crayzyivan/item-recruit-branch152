package com.item.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Save recommendation data transfer object
 * Used for saving recommendation records to database
 *
 * @author hua.liu
 * @since 2025-10-23
 */
@Data
public class SaveRecommendationDTO {

    private Long jobId;

    private Long candidateId;

    private Long recommendBy;

    private String candidateEmail;

    private LocalDateTime recommendTime;

    private String recommendReasons;
}

