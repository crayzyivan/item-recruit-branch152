package com.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : lh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerQuestion5SInfoDTO {
    private String scoreFor5S;
    private String answerConfirmDateTime;
}
