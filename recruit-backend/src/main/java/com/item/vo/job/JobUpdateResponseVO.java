package com.item.vo.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * update save job response vo
 *
 * @author : lh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobUpdateResponseVO {
    /**
     * is exist similar job
     */
    private boolean existSimilarJob;
}
