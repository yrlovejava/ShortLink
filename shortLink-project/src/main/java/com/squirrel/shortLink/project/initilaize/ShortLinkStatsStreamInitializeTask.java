package com.squirrel.shortLink.project.initilaize;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.squirrel.shortLink.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_GROUP_KEY;
import static com.squirrel.shortLink.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY;

/**
 * 初始化短链接监控消息队列消费者组
 */
@Component
@RequiredArgsConstructor
public class ShortLinkStatsStreamInitializeTask implements InitializingBean {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 初始化消费者组
     * @throws Exception 可能抛出的异常
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Boolean hasKey = stringRedisTemplate.hasKey(SHORT_LINK_STATS_STREAM_TOPIC_KEY);
        if (hasKey == null || Boolean.FALSE.equals(hasKey)) {
            stringRedisTemplate.opsForStream().createGroup(SHORT_LINK_STATS_STREAM_TOPIC_KEY,SHORT_LINK_STATS_STREAM_GROUP_KEY);
        }
    }
}
