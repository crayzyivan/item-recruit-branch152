package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 积分按分钟计费配置
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-15  15:33
 */
@RefreshScope
@Component
@Data
@ConfigurationProperties(prefix = "recruit.minute-based")
public class MinuteBasedConfig {
    //1分对应1积分
    private Integer centExchangeRate;
    //就绪扣除1费用
    private BigDecimal readyDeductAmount;
    //ai面试冻结分钟数
    private Integer interviewFreezeMinutes;
    //面试每分钟消耗费用
    private BigDecimal interviewCostPerMinute;
    //音频面试每分钟消耗费用
    private BigDecimal audioInterviewCostPerMinute;
}