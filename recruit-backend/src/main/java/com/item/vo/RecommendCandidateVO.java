package com.item.vo;

import lombok.Data;

/**
 * 推荐候选人
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-23  13:47
 */
@Data
public class RecommendCandidateVO {
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String cityName;
    private String stateName;
    private String countryName;

}