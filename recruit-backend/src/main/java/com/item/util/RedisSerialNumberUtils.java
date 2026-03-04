package com.item.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 生成积分交易流水号
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  14:23
 */
@Component
@Slf4j
public class RedisSerialNumberUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成流水号
     * 格式: [prefix][yyyyMMdd][8位递增流水号]
     * 例如: DL2025072900000001
     *
     * @param prefix       业务前缀，如 DL、RC 等
     * @param expireDays   Redis Key 过期天数，默认 1 天
     * @return 流水号
     */
    public String generate(String prefix, int expireDays) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = String.format("serial:%s:%s", prefix, date); // 如 serial:DL:20250729

        Long increment = redisTemplate.opsForValue().increment(redisKey);

        if (increment != null && increment == 1) {
            redisTemplate.expire(redisKey, Duration.ofDays(expireDays));
        }

        return String.format("%s%s%08d", prefix, date, increment);
    }

    /**
     * 重载方法，默认过期1天
     */
    public String generate(String prefix) {
        return generate(prefix, 1);
    }
}