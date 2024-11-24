package com.squirrel.project.mq.consumer;

import com.squirrel.project.dto.biz.ShortLinkStatsRecordDTO;
import com.squirrel.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static com.squirrel.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATUS_KEY;

/**
 * 延迟记录短链接统计组件
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;

    /**
     * 延迟记录短链接
     */
    public void onMessage() {
        // 单线程线程池
        Executors.newSingleThreadExecutor(
                runnable -> {
                    Thread r = new Thread(runnable);
                    r.setName("delay_short_link_stats_consumer");
                    // 设置为守护线程
                    r.setDaemon(Boolean.TRUE);
                    return r;
                })
        .execute(() -> {
            // 使用redisson的阻塞队列
            RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATUS_KEY);
            // 使用redisson的延时队列
            RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            // 自旋
            for(;;){
                try {
                    ShortLinkStatsRecordDTO statsRecord = delayedQueue.poll();
                    if (statsRecord != null) {
                        shortLinkService.shortLinkStats(null,null,statsRecord);
                        continue;
                    }
                    // 休眠500ms
                    LockSupport.parkUntil(500);
                }catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 在bean所有属性设置完成后开始监听延迟队列
     * @throws Exception 可能抛出的异常
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();
    }
}
