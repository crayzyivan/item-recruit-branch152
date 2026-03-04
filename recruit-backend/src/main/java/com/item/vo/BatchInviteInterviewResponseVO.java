package com.item.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量邀请面试响应VO
 * 返回批量处理的结果统计和详细信息
 *
 * @since 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchInviteInterviewResponseVO {
    
    /**
     * 总处理数量
     */
    private Integer totalCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failureCount;
    
    /**
     * 成功的邀请详情列表
     */
    private List<BatchInviteInterviewItemVO> successList;
    
    /**
     * 失败的邀请详情列表
     */
    private List<BatchInviteInterviewItemVO> failureList;
    
    /**
     * 批量处理总耗时（毫秒）
     */
    private Long processingTimeMs;
}
