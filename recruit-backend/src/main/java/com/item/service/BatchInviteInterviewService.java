package com.item.service;

import com.item.vo.BatchInviteInterviewRequestVO;
import com.item.vo.BatchInviteInterviewResponseVO;

public interface BatchInviteInterviewService {

    /**
     * 批量邀请面试
     * 使用CompletableFuture并行处理多个候选人的邀请，提高处理效率
     * 每个邀请独立处理，失败不影响其他邀请
     *
     * @param request 批量邀请面试请求
     * @return 批量邀请面试响应，包含成功和失败的详细信息
     */
    BatchInviteInterviewResponseVO batchInviteInterview(BatchInviteInterviewRequestVO request);

}
