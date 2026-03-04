package com.item.framework.constant;

/**
 * 招聘申请状态 枚举
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-18  16:28
 */
public enum JobApplyStatus {
    SUBMITTED(0, "SUBMITTED"),//已提交
    SCREENED(10, "SCREENED"),//AI筛选
    VETTED(20, "VETTED"),//AI面试
    REVIEW(30, "REVIEW"),//人工审核
    MANUAL_REVIEW(35, "MANUAL_REVIEW"),//自动流程面试未通过到人工审核
    READY(40, "READY"),//就绪
    BACKGROUND(50, "BACKGROUND"),//背景审核
    DENIED(99, "DENIED"),//拒绝
    ACCEPTED(100, "ACCEPTED");//录用

    private final int code;
    private final String name;

    JobApplyStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static JobApplyStatus fromCode(int code) {
        for (JobApplyStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid JobApplyStatus code: " + code);
    }
}
