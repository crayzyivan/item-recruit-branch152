package com.item.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 候选人得分情况
 * 包含评估、面试、监考、技能和软技能等5个维度的分数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateScores {
    
    /**
     * 面试分数
     */
    private Integer interviewScore;
    
    /**
     * 监考分数
     */
    private Integer proctoringScore;
    
    /**
     * 技能分数
     */
    private Integer technicalSkillScore;
    
    /**
     * 软技能分数
     */
    private Integer softSkillScore;

    /**
     * 根据面试分数、监考分数、技能分数和软技能分数根据权重计算得出。
     */
    private Integer totalScore;
}
