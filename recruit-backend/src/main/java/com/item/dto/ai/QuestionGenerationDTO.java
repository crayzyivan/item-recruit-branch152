package com.item.dto.ai;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * <p>
 * AI面试问题
 * </p>
 *
 * @author liuyabin on 2025/7/19
 * @since 1.0.0
 */
@Data
public class QuestionGenerationDTO implements Serializable {
    private Integer number;
    private String context = "";
    private List<String> dimensions;
}
