package com.item.dto.job;

import lombok.Data;

@Data
public class CandidateJobSearchDto {
    private Long candidateId;
    private Long jobId;
    private Integer applyStatus;
    private Integer posted;
} 