package com.item.service;

import com.item.vo.InviteInterviewRequestVO;
import com.item.vo.InviteInterviewResponseVO;

/**
 * 邀请面试服务接口
 * 用于HR邀请外部候选人面试
 *
 * @since 2025-11-13
 */
public interface InviteInterviewService {
    
    /**
     * 邀请面试
     * 支持简历解析、IAM账号创建、职位申请创建和简历匹配流程
     *
     * @param request 邀请面试请求
     * @return 邀请面试响应
     */
    InviteInterviewResponseVO inviteInterview(InviteInterviewRequestVO request);

}

