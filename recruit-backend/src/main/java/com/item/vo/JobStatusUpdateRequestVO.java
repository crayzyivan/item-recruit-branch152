package com.item.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Job status update request VO
 *
 * @author system
 * @since 1.0.0
 */
@Data
public class JobStatusUpdateRequestVO {
    
    /**
     * Job ID (required)
     */
    @NotNull(message = "Job ID is required")
    @Positive(message = "Job ID must be positive")
    private Long jobId;
    
    /**
     * Job status (required)
     * Valid values: 0=Draft, 1=Active, 2=Closed, 3=Other, 4=Awaiting Payment, 5=On Hold, 6=Pending Review, 7=Pending Publication, 8=Pending Modification
     */
    @NotNull(message = "Job status is required")
    @Min(value = 0, message = "Job status must be positive")
    @Max(value = 8, message = "Job status must be between 0-8")
    private Integer jobStatus;
    
    /**
     * Optional comment for the status change
     */
    @Size(max = 10000, message = "Comment must not exceed 10000 characters")
    private String comment;
} 