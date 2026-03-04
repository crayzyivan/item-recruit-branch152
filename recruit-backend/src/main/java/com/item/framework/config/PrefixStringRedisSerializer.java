package com.item.framework.config;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 统一加前缀
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  14:41
 */
public class PrefixStringRedisSerializer implements RedisSerializer<String> {

    private final String prefix;
    private final StringRedisSerializer delegate = new StringRedisSerializer();

        public PrefixStringRedisSerializer(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public byte[] serialize(String key) throws SerializationException {
        return delegate.serialize(prefix + key);
    }

    @Override
    public String deserialize(byte[] bytes) throws SerializationException {
        return delegate.deserialize(bytes);
    }
}