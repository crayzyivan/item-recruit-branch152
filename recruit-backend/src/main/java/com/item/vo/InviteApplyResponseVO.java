package com.item.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邀请投递响应VO
 * 返回候选人ID、IAM账号等信息
 *
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteApplyResponseVO {


    /**
     * IAM账号（邮箱）
     */
    private String iamAccount;

    /**
     * 响应消息
     */
    private String message;
}

