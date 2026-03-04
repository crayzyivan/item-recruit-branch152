package com.item.vo;

import lombok.Data;

/**
 * 候选人各项评分
 *
 * @author liyunlong
 * @version 1.0
 * @since 2026-01-08  15:08
 */
@Data
public class CandidateScoreVO {

    //简历分数
    private Integer resume;

    //面试分数
    private Integer interview;
    private Integer proctoring;
    private Integer technicalSkill;
    private Integer softSkill;
    private Integer question5S;
    private Integer overall;

}