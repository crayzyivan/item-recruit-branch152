package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author : lh
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "recruit.business.deduction.point")
public class BusinessDeductionPointsConfig {
    private Point publishJob = new Point();
    private Point aiInterview = new Point();
    private Point backgroundCheck = new Point();
    private Point resumeScreen = new Point();
    private Point downloadResume = new Point();

    @Data
    public static class Point {
        //扣减积分
        private int deductedPoints;
        //占用积分时间
        private int freezeExpireHours;
        //面试占用积分
        private int interviewFreezePoints;
        //邮件扣减积分
        private int emailDeductedPoints;
    }

}
