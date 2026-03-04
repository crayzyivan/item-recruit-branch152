package com.item.dto.ai;

import lombok.Data;

@Data
public class SendInterviewMailDTO {
    private Long jobId;
    private Long candidateId;
    private Long candidateJobId;
}
