package com.item.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 候选人得分权重配置
 * 权重采用百分制，例如：50表示50%，80表示80%
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateScoreWeights {
    
    /**
     * 面试分数权重（百分制）
     */
    private Integer interviewWeight;
    
    /**
     * 监考分数权重（百分制）
     */
    private Integer proctoringWeight;
    
    /**
     * 技能分数权重（百分制）
     */
    private Integer technicalSkillWeight;
    
    /**
     * 软技能分数权重（百分制）
     */
    private Integer softSkillWeight;
}
