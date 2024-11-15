package com.squirrel.shortLink.admin.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置
 */
@Configuration
public class RBloomFilterConfiguration {

    /**
     * 用户注册的布隆过滤器
     * @param redissonClient redisson 客户端
     * @return RBloomFilter<String>
     */
    @Bean
    public RBloomFilter<String> userRegisterCachePenetrationBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter("userRegisterPenetrationBloomFilter");
        cachePenetrationBloomFilter.tryInit(
                100000000L,// 布隆过滤器的容量 1亿
                0.001 // 误判率 0.1%的假阳性
        );
        return cachePenetrationBloomFilter;
    }
}
