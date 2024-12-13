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
package com.squirrel.shortLink.project.mq.producer;

import cn.hutool.core.lang.UUID;
import com.squirrel.shortLink.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.squirrel.shortLink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATUS_KEY;

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
        // 设置消息的唯一id
        statsRecord.setKeys(UUID.fastUUID().toString());
        // 获取 redisson 的阻塞队列
        RBlockingQueue<ShortLinkStatsRecordDTO> blockingQueue = redissonClient.getBlockingQueue(DELAY_QUEUE_STATUS_KEY);
        // 获取 redisson 延迟队列
        RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        // 5s 后将消息发送到指定队列
        delayedQueue.offer(statsRecord, 5, TimeUnit.SECONDS);
    }
}
