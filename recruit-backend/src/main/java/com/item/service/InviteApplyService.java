package com.item.service;

import com.item.vo.InviteApplyRequestVO;
import com.item.vo.InviteApplyResponseVO;

/**
 * 邀请投递服务接口
 * 用于HR邀请外部候选人投递职位
 *
 * @since 2025-11-13
 */
public interface InviteApplyService {

    /**
     * 邀请投递
     * 实现HR邀请外部候选人投递职位的核心业务逻辑。
     * 包括：参数验证、职位验证、候选人创建或获取、IAM账号创建、发送邀请投递邮件。
     *
     * @param request 邀请投递请求VO
     * @return 邀请投递响应VO
     */
    InviteApplyResponseVO inviteApply(InviteApplyRequestVO request);
}

