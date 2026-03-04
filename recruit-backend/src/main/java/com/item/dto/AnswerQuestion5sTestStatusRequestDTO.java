package com.item.dto;

import com.item.framework.annotation.Xss;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class AnswerQuestion5sTestStatusRequestDTO {
    @Xss
    private String dateTime;
    @Xss
    private String candidateJobId;
    @Xss
    private String signature;
}
