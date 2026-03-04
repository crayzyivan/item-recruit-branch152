package com.item.dto;

import com.item.framework.annotation.Xss;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class AnswerQuestion5sTestConfirmRequestDTO {
    @Xss
    private String dateTime;
    @Xss
    private String candidateJobId;
    @Xss
    private String signature;
    @Xss
    @NotNull(message = "Score must not be null")
    @NotBlank(message = "Score must not be blank")
    private String score;
}
