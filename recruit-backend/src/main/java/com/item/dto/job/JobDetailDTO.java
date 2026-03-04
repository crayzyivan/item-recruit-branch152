package com.item.dto.job;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobDetailDTO {
    @NotNull
    @Min(1)
    private Long jobId;
} 