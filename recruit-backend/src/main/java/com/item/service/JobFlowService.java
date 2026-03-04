package com.item.service;

import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.framework.constant.JobStatus;

/**
 * 招聘状态流转
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  16:34
 */
public interface JobFlowService {

    /**
     * 更新招聘流程状态
     * @param CandidateJobId  候选人职位关联表id
     * @param jobApplyStatusEvent 更新招聘状态事件
     * @return
     */
    public void fireEvent(Long CandidateJobId, JobApplyStatusEvent jobApplyStatusEvent);

}