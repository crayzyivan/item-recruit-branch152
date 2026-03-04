package com.item.framework.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * redis缓存配置
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-21  15:27
 */
@EnableCaching
@Configuration
public class RedisCacheConfig {

    //缓存key前缀
    @Value("${redis.prefix}")
    private String prefix;
    // 基础 TTL（秒）
    @Value("${redis.base.ttl:1800}")
    private Long baseTtl;
    // 随机偏移上限（秒）
    @Value("${redis.random.bound:600}")
    private Long randomBound;

    @Resource
    private RecruitCommonNacosConfig recruitCommonNacosConfig;

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheWriter writer = RedisCacheWriter.nonLockingRedisCacheWriter(factory);
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(new RandomTtlFunction(baseTtl,randomBound)) //随机有效期
                .prefixCacheNameWith(prefix)//统一前缀
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()  // 使用JSON序列化
                ));
        return RedisCacheManager.builder(writer).cacheDefaults(config).build();
    }

    /**
     * 统一加前缀
     * @param factory
     * @return
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        // 自定义 Key 序列化器，自动加上前缀
        template.setKeySerializer(new PrefixStringRedisSerializer(prefix));
        return template;
    }

    @Bean(value = "companyInfoCacheManager")
    public RedisCacheManager companyInfoCacheManager(RedisConnectionFactory factory) {
        RedisCacheWriter writer = RedisCacheWriter.nonLockingRedisCacheWriter(factory);
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                //随机有效期
                .entryTtl(new RandomTtlFunction(recruitCommonNacosConfig.getCacheCompanySeconds(),recruitCommonNacosConfig.getCacheCompanySecondsRandom()))
                //统一前缀
                .prefixCacheNameWith(prefix + "company_")
                // 使用JSON序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                ));
        return RedisCacheManager.builder(writer).cacheDefaults(config).build();
    }

}