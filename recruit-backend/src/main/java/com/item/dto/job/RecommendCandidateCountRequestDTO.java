package com.item.dto.job;

import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class RecommendCandidateCountRequestDTO {
    private List<Long> jobIds;
}
