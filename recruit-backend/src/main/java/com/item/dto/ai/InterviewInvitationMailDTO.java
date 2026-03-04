package com.item.dto.ai;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InterviewInvitationMailDTO {
    private String jobTitle;
    private String companyName;
    private String candidateName;
    private String interviewUrl;
    private String startDate;
    private String endDate;
    private String time;
    private long interviewTimeLimit;
    /**
     * 是否显示账号密码信息
     */
    private Boolean showAccountInfo;
    /**
     * 账号（邮箱）
     */
    private String account;
    /**
     * 初始密码
     */
    private String password;
    /**
     * 平台地址
     */
    private String platformUrl;

}
