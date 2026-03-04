package com.item.vo;

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
public class RecommendCandidateCountVO {
    /**
     * 工作岗位id
     */
    private Long jobId;

    /**
     * 推荐的候选人数量 默认0
     */
    private int recommendCandidateCount;

}
