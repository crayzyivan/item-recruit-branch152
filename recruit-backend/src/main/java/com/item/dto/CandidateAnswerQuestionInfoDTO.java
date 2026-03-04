package com.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : lh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateAnswerQuestionInfoDTO {

    private AnswerQuestion5SInfoDTO answerQuestion5S;
}
