package com.item.dto.ai;

import com.item.vo.ai.AiCallbackDataVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCallbackDTO {

    private Long id;

    /**
     * 回调事件
     */
    private String event;

    /**
     * 事件AI处理完成时间
     */
    private LocalDateTime timestamp;

    /**
     * AI面试ID
     */
    private String interviewId;

    /**
     * 面试者邮箱
     */
    private String interviewerEmail;

    /**
     * 面试者面试ID
     */
    private String callId;

    private Long candidateJobId;

    private AiCallbackDataVO data;

}
