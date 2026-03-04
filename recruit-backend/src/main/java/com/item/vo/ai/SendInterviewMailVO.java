package com.item.vo.ai;

import lombok.Data;

@Data
public class SendInterviewMailVO {
    private Long jobId;
    private Long candidateId;
    private Long candidateJobId;
}
