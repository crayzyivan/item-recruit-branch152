package com.item.vo.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * publish job response
 *
 * @author : lh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCreateResponseVO {

    /**
     * current job id
     */
    private Long jobId;

    /**
     * is exist similar job
     */
    private boolean existSimilarJob;

}
