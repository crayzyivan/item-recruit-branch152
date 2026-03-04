package com.item.framework.constant;

/**
 * 更新招聘状态事件 枚举
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  16:28
 */
public enum JobApplyStatusEvent {
    //简历提交
    SUBMIT,
    //调用AI筛选
    SCREEN,
    //进行AI面试
    VETTED,
    //AI面试界面-通过操作
    AI_PASS,
    //自动流程 AI通过到就绪
    AUTO_AI_TO_READY,
    //自动流程 AI不通过到人工审核
    AUTO_AI_TO_REVIEW,
    //自动流程 人工审核界面-通过操作
    AUTO_REVIEW_PASS,
    //人工审核界面-通过操作
    REVIEW_PASS,
    //就绪界面-调用背景审核
    BACKGROUND_CHECK,
    //拒绝操作
    REJECT
}
