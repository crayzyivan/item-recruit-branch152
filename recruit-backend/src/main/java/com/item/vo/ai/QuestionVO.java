package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Question evaluation object
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionVO
{
    /**
     * Question text
     */
    private String question;

    /**
     * Question score
     */
    private Integer score;

    /**
     * Question feedback
     */
    private String feedback;
}

