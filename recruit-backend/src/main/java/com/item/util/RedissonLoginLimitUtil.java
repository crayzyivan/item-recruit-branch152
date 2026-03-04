package com.item.util;

import jakarta.annotation.Resource;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLoginLimitUtil {

    @Value("${redis.prefix}")
    private String prefix;
    private String loginFailPrefix = "login_fail:";
    private String loginLockPrefix = "login_lock:";
    @Value("${login.max_fail_count}")
    private int maxFailCount;
    @Value("${login.lock_minutes}")
    private int lockMinutes;

    @Resource
    private RedissonClient redissonClient;

    private String getLoginFailPrefix() {
        return prefix + loginFailPrefix;
    }

    private String getLoginLockPrefix() {
        return prefix + loginLockPrefix;
    }

    /**
     * 检查是否被锁定
     */
    public boolean isLocked(String email) {
        RBucket<Boolean> lockBucket = redissonClient.getBucket(getLoginLockPrefix() + email);
        return lockBucket.isExists();
    }

    /**
     * 记录登录失败
     */
    public void recordLoginFail(String email) {
        String failKey = getLoginFailPrefix() + email;
        String lockKey = getLoginLockPrefix() + email;

        // 获取当前失败次数
        RAtomicLong failCount = redissonClient.getAtomicLong(failKey);
        long currentCount = failCount.incrementAndGet();

        // 设置失败记录的过期时间
        failCount.expire(lockMinutes, TimeUnit.MINUTES);

        // 如果达到最大失败次数，锁定账号
        if (currentCount >= maxFailCount) {
            RBucket<Boolean> lockBucket = redissonClient.getBucket(lockKey);
            lockBucket.set(true, lockMinutes, TimeUnit.MINUTES);
            // 清除失败次数记录
            failCount.delete();
        }
    }

    /**
     * 登录成功后清除失败记录
     */
    public void clearLoginFail(String email) {
        String failKey = getLoginFailPrefix() + email;
        String lockKey = getLoginLockPrefix() + email;

        redissonClient.getAtomicLong(failKey).delete();
        redissonClient.getBucket(lockKey).delete();
    }

    /**
     * 获取剩余锁定时间（分钟）
     */
    public long getRemainingLockTime(String email) {
        RBucket<Boolean> lockBucket = redissonClient.getBucket(getLoginLockPrefix() + email);
        if (lockBucket.isExists()) {
            return lockBucket.remainTimeToLive() / (1000 * 60); // 转换为分钟
        }
        return 0;
    }

    /**
     * 获取当前失败次数
     */
    public long getFailCount(String email) {
        RAtomicLong failCount = redissonClient.getAtomicLong(getLoginFailPrefix() + email);
        return failCount.isExists() ? failCount.get() : 0;
    }

    /**
     * 获取剩余尝试次数
     */
    public long getRemainingAttempts(String email) {
        long currentFailCount = getFailCount(email);
        return Math.max(0, maxFailCount - currentFailCount);
    }
}