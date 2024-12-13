/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squirrel.shortLink.admin.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置
 */
@Configuration(value = "rBloomFilterConfigurationByAdmin")
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

    /**
     * 防止分组标识注册查询数据库的布隆过滤器
     * @param redissonClient redisson 客户端
     * @return RBloomFilter<String>
     */
    public RBloomFilter<String> gidRegisterCachePenetrationBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter("gidRegisterCachePenetrationBloomFilter");
        cachePenetrationBloomFilter.tryInit(
                200000000L,// 布隆过滤器的容量 2亿
                0.001 // 误判率 0.1%的假阳性
        );
        return cachePenetrationBloomFilter;
    }
}
