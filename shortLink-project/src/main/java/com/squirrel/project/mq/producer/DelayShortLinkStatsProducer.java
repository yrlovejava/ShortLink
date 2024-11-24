package com.squirrel.project.mq.producer;

import com.squirrel.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.squirrel.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATUS_KEY;

/**
 * 延迟消费短链接统计发送者
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsProducer {

    private final RedissonClient redissonClient;

    /**
     * 发送延迟消费短链接统计
     * @param statsRecord 短链接统计实体参数
     */
    public void send(ShortLinkStatsRecordDTO statsRecord) {
        // 获取 redisson 的阻塞队列
        RBlockingQueue<ShortLinkStatsRecordDTO> blockingQueue = redissonClient.getBlockingQueue(DELAY_QUEUE_STATUS_KEY);
        // 获取 redisson 延迟队列
        RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        // 5s 后将消息发送到指定队列
        delayedQueue.offer(statsRecord, 5, TimeUnit.SECONDS);
    }
}
