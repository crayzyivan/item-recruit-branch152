package com.item.service.impl;

import com.item.framework.config.VerificationConfig;
import static com.item.framework.constant.CommonConstants.NumConstants.SECONDS_MILL;
import static com.item.framework.constant.CommonResponseCode.COMMON_ERROR_MANY_ACCOUNT_LOCK;
import static com.item.framework.constant.CommonResponseCode.COMMON_ERROR_MANY_LOCK;
import static com.item.framework.constant.CommonResponseCode.COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER;
import static com.item.framework.constant.CommonResponseCode.COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER_SECONDS;
import static com.item.framework.constant.CommonResponseCode.COMMON_VERIFY_CODE_NULL;
import com.item.framework.error.BusinessException;
import com.item.service.VerifyCodeService;
import com.item.util.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyCodeService {
    // Redis Key 模板
    // type:email
    private static final String CODE_KEY_TEMPLATE = "verification:code:%s:%s";
    private static final String ATTEMPTS_KEY_TEMPLATE = "verification:attempts:%s:%s";
    private static final String LAST_SEND_KEY_TEMPLATE = "verification:lastSend:%s:%s";
    private static final String LOCK_KEY_TEMPLATE = "lock:verification:%s:%s";

    private final MailUtils mailUtils;
    private final RedissonClient redissonClient;
    private final VerificationConfig verificationConfig;

    /**
     * 发送验证码 (支持多场景)
     *
     * @param email 邮箱地址
     * @param type 场景类型 (register/forgot-password)
     */
    @Override
    public void sendVerificationCode(String email, String type) {;
        // 获取专用锁 (场景+邮箱)
        RLock lock = getLock(email, type);
        try {
            lock.lock();
            // 检查锁定状态
            if (isLocked(email, type)) {
                throw BusinessException.of(COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER);
            }

            // 检查发送间隔
            long lastSentTime = getLastSendTime(email, type);
            long currentTime = System.currentTimeMillis();
            long cooldown = lastSentTime + (verificationConfig.getSendInterval() * SECONDS_MILL) - currentTime;

            if (lastSentTime > 0 && cooldown > 0) {
                throw BusinessException.of(COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER_SECONDS.getCode(),
                        String.format(COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER_SECONDS.getMsg(), cooldown / SECONDS_MILL));
            }

            // 生成验证码
            String code = generateCode();

            // 存储验证码
            String codeKey = getKey(CODE_KEY_TEMPLATE, type, email);
            RBucket<String> codeBucket = redissonClient.getBucket(codeKey);

            codeBucket.set(code, Duration.ofMinutes(verificationConfig.getCodeExpiration()));

            // 更新发送时间
            updateLastSendTime(email, type, currentTime);

            // 发送邮件 (示例 - 生产环境替换为真实邮件服务)
//            EmailVerificationConfig.ScenarioConfig

        } finally {
            lock.unlock();
        }
    }

    /**
     * 验证验证码
     *
     * @param email 邮箱
     * @param code 用户输入的验证码
     * @param type 场景类型
     * @return 验证是否成功
     */
    @Override
    public boolean verifyCode(String email, String code, String type) {
        // 获取专用锁
        RLock lock = getLock(email, type);
        try {
            lock.lock();

            // 检查锁定状态
            if (isLocked(email, type)) {
                throw BusinessException.of(COMMON_ERROR_MANY_ACCOUNT_LOCK);
            }

            // 获取存储的验证码
            String codeKey = getKey(CODE_KEY_TEMPLATE, type, email);
            RBucket<String> codeBucket = redissonClient.getBucket(codeKey);
            String storedCode = codeBucket.get();

            // 验证码不存在或已过期
            if (storedCode == null) {
                throw BusinessException.of(COMMON_VERIFY_CODE_NULL);
            }

            // 验证码匹配
            if (StringUtils.equalsIgnoreCase(storedCode, code)) {
                // 验证成功
                // 删除验证码
                codeBucket.delete();
                // 重置错误计数
                resetAttempts(email, type);
                return true;
            }

            // 验证失败 - 处理错误计数
            handleFailedAttempt(email, type, codeKey);
            return false;

        } finally {
            lock.unlock();
        }
    }

    // 生成验证码
    private String generateCode() {
        String charset = verificationConfig.getCharsetString();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return random.ints(verificationConfig.getCodeLength(), 0, charset.length())
                .mapToObj(charset::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    // 处理验证失败
    @Override
    public void handleFailedAttempt(String email, String type, String codeKey) {
        String attemptsKey = getKey(ATTEMPTS_KEY_TEMPLATE, type, email);
        RAtomicLong counter = redissonClient.getAtomicLong(attemptsKey);

        // 原子增加计数
        long newCount = counter.incrementAndGet();

        // 达到最大错误次数
        if (newCount >= verificationConfig.getMaxErrorCount()) {
            // 锁定账户
            counter.expire(Duration.ofMinutes(verificationConfig.getLockDuration()));

            // 使验证码失效
            redissonClient.getBucket(codeKey).delete();

            throw BusinessException.of(COMMON_ERROR_MANY_LOCK.getCode(), String.format(COMMON_ERROR_MANY_LOCK.getMsg(), verificationConfig.getLockDuration()));
        }
    }

    // 检查是否被锁定
    private boolean isLocked(String email, String type) {
        String attemptsKey = getKey(ATTEMPTS_KEY_TEMPLATE, type, email);
        RAtomicLong counter = redissonClient.getAtomicLong(attemptsKey);
        return counter.isExists() && counter.get() >= verificationConfig.getMaxErrorCount();
    }

    // 获取上次发送时间
    private long getLastSendTime(String email, String type) {
        String lastSendKey = getKey(LAST_SEND_KEY_TEMPLATE, type, email);
        RAtomicLong lastSent = redissonClient.getAtomicLong(lastSendKey);
        return lastSent.get();
    }

    // 更新上次发送时间
    private void updateLastSendTime(String email, String type, long time) {
        String lastSendKey = getKey(LAST_SEND_KEY_TEMPLATE, type, email);
        RAtomicLong lastSent = redissonClient.getAtomicLong(lastSendKey);
        lastSent.expire(Duration.ofMinutes(verificationConfig.getCodeExpiration()+3));
        lastSent.set(time);
    }

    // 重置错误计数
    private void resetAttempts(String email, String type) {
        String attemptsKey = getKey(ATTEMPTS_KEY_TEMPLATE, type, email);
        redissonClient.getAtomicLong(attemptsKey).delete();
    }

    // 获取分布式锁
    private RLock getLock(String email, String type) {
        String lockKey = String.format(LOCK_KEY_TEMPLATE, type, email);
        return redissonClient.getLock(lockKey);
    }

    // 生成Redis Key
    private String getKey(String template, String type, String email) {
        return String.format(template, type, email);
    }
}
