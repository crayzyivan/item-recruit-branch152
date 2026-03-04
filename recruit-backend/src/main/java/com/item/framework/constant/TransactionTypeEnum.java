package com.item.framework.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分类型
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  14:02
 */
@Getter
@AllArgsConstructor
public enum TransactionTypeEnum {
    TOP_UP(0),//充值
    TRANSFER_OUT(10),//转出
    TRANSFER_IN(20),//转入
    RESUME_DOWNLOAD(30),//下载简历
    PUBLISH_JOB(40),//发布职位
    RESUME_SCREEN(50),//简历筛选
    INTERVIEW_MAIL(60),//面试邮件
    AI_INTERVIEW(70),//AI面试
    BACKGROUND_CHECK(80),//背景调查
    READY(90);//就绪

    private final int code;


}