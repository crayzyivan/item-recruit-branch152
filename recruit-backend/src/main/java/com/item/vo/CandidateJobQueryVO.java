package com.item.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 候选人职位查询参数
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  17:33
 */
@Data
@Builder
public class CandidateJobQueryVO {
    private Integer pageIndex;
    private Integer pageSize;
    //职位id
    private Long jobId;

    private String companyCode;
    //招聘状态
    private Integer applyStatus;

    private String candidateName;

    //排序字段
    List<SortFieldVO> sortFields;

    private Integer score;
}