package com.item.framework.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分交易流水号前缀
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  14:02
 */
@Getter
@AllArgsConstructor
public enum TransactionNoTypeEnum {
    TOP_UP("TU"),////充值
    TRANSFER("TF"),//转移
    RESUME_DOWNLOAD("DL"),//下载简历
    PUBLISH_JOB("PJ"),//发布职位
    RESUME_SCREEN("RS"),//简历筛选
    INTERVIEW_MAIL("IM"),//面试邮件
    AI_INTERVIEW("AI"),//AI面试
    BACKGROUND_CHECK("BG"),//背景调查
    READY("RD");//就绪

    private final String code;


}