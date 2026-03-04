package com.item.framework.config;

import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 动态生成redis缓存过期事件
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-22  11:31
 */
public class RandomTtlFunction implements RedisCacheWriter.TtlFunction {

    // 基础 TTL（秒）
    private final long baseTtl;
    // 随机偏移上限（秒）
    private final long randomBound;  // 随机偏移上限（秒）

    public RandomTtlFunction(long baseTtl, long randomBound) {
        this.baseTtl = baseTtl;
        this.randomBound = randomBound;
    }

    @Override
    public Duration getTimeToLive(Object key, @Nullable Object value) {
        long ttl = baseTtl + ThreadLocalRandom.current().nextLong(randomBound);
        return Duration.ofSeconds(ttl);
    }
}