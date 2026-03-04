package com.item.dto.job;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Random job recommendation request DTO
 * Used for encapsulating parameters for random job recommendation API
 *
 * @author hua.liu
 */
@Data
public class RandomJobRecommendRequestDTO {
    
    /**
     * Number of jobs to recommend (required) default 5
     * Range: 1-50
     */
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 50, message = "Size must not exceed 50")
    private Integer size = 5;
    
    /**
     * Company code for filtering jobs (optional)
     * If provided, only jobs from this company will be recommended
     */
    private String companyCode;
    
    /**
     * Random seed for reproducible results (optional)
     * If provided, same seed will generate same random results
     */
    private Long seed;
    
    /**
     * Job category ID for filtering (required)
     * If provided, only jobs from these categories will be recommended
     */
    @NotNull(message = "Category must not be null")
    @Min(value = 1, message ="Category must be greater than or equal to {value}")
    private Long categoryId;
    
    /**
     * Locations for filtering (required)
     * If provided, only jobs from these locations will be recommended
     */
    @Valid
    @NotNull(message = "Locations must not be null")
    @Size(min = 1, max = 15, message = "Locations size must be between {min} and {max}")
    private List<LocationValDTO> locations;

    @NotNull(message = "Exclude job id id must not be null")
    @Min(value = 1, message ="Exclude job id must be greater than or equal to {value}")
    private Long excludeJobId;
}
